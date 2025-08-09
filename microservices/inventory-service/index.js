const amqp = require('amqplib');
const mysql = require('mysql2/promise');

// Configuración usando variables de entorno
const RABBITMQ_URL = `amqp://${process.env.RABBITMQ_USER}:${process.env.RABBITMQ_PASSWORD}@${process.env.RABBITMQ_HOST}`;
const DB_CONFIG = {
  host: process.env.MYSQL_HOST,
  user: process.env.MYSQL_USER,
  password: process.env.MYSQL_PASSWORD,
  database: process.env.MYSQL_DB
};

async function startConsumer() {
    try {
        const connection = await amqp.connect(RABBITMQ_URL);
        const channel = await connection.createChannel();
        const exchangeName = 'agroflow-exchange';
        const queueName = 'cola_inventario';

        await channel.assertExchange(exchangeName, 'direct', { durable: true });
        await channel.assertQueue(queueName, { durable: true });
        await channel.bindQueue(queueName, exchangeName, 'nueva_cosecha');

        console.log('Esperando mensajes de nuevas cosechas...');
        channel.consume(queueName, async (msg) => {
            if (msg !== null) {
                const cosecha = JSON.parse(msg.content.toString());
                console.log(`Recibida cosecha para inventario: ${cosecha.id}`);

                try {
                    const insumosNecesarios = {
                        'arroz': { 'semilla': 5, 'fertilizante': 2 },
                        'maiz': { 'semilla': 4, 'fertilizante': 3 }
                    };
                    const insumos = insumosNecesarios[cosecha.tipoProducto];

                    if (insumos) {
                        const dbConnection = await mysql.createConnection(DB_CONFIG);
                        for (const insumo in insumos) {
                            const cantidadADescontar = cosecha.cantidadKg * insumos[insumo];
                            const query = `UPDATE insumos SET stock = stock - ? WHERE nombre = ?`;
                            await dbConnection.execute(query, [cantidadADescontar, insumo]);
                            console.log(`Descontado ${cantidadADescontar} kg de ${insumo}.`);
                        }
                        await dbConnection.end();
                        channel.ack(msg);
                    } else {
                        console.log('Tipo de producto no reconocido. Mensaje rechazado.');
                        channel.nack(msg, false, false);
                    }
                } catch (error) {
                    console.error('Error al procesar el mensaje:', error);
                    channel.nack(msg, false, true); // Re-envía el mensaje
                }
            }
        });
    } catch (error) {
        console.error('Error de conexión a RabbitMQ:', error);
        setTimeout(startConsumer, 5000); // Intenta reconectar
    }
}

startConsumer();