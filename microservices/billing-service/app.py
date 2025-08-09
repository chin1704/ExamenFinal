import pika
import mysql.connector
import json
import requests
import os
import threading

RABBITMQ_HOST = os.getenv('RABBITMQ_HOST')
RABBITMQ_USER = os.getenv('RABBITMQ_USER')
RABBITMQ_PASSWORD = os.getenv('RABBITMQ_PASSWORD')

DB_CONFIG = {
    'host': os.getenv('MARIADB_HOST'),
    'user': os.getenv('MARIADB_USER'),
    'password': os.getenv('MARIADB_PASSWORD'),
    'database': os.getenv('MARIADB_DB')
}

PRICES = {
    'arroz': 120, # USD por tonelada
    'maiz': 90
}

def callback(ch, method, properties, body):
    try:
        cosecha = json.loads(body)
        print(f"Recibida cosecha para facturar: {cosecha['id']}")

        tipo_producto = cosecha.get('tipoProducto')
        cantidad = cosecha.get('cantidadKg')

        monto_total = (cantidad / 1000) * PRICES.get(tipo_producto, 0)

        # Conexión a MariaDB
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("INSERT INTO facturas (cosecha_id, agricultor_id, monto) VALUES (%s, %s, %s)", 
                       (cosecha['id'], cosecha['agricultorId'], monto_total))
        conn.commit()
        conn.close()

        print(f"Factura generada: ${monto_total} USD.")

        # Notificación al servicio central
        central_api_url = f"http://{os.getenv('CENTRAL_SERVICE_HOST')}:{os.getenv('CENTRAL_SERVICE_PORT')}/api/v1/cosechas/{cosecha['id']}/estado"
        response = requests.put(central_api_url, data='FACTURADA', headers={'Content-Type': 'text/plain'})
        if response.status_code == 200:
            print("Estado de cosecha actualizado en servicio central.")
        else:
            print(f"Error al notificar al servicio central: {response.status_code}")

        ch.basic_ack(method.delivery_tag)
    except Exception as e:
        print(f"Error procesando mensaje: {e}")
        ch.basic_nack(method.delivery_tag, requeue=True)

def start_consumer():
    connection = pika.BlockingConnection(pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        credentials=pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
    ))
    channel = connection.channel()
    channel.queue_declare(queue='cola_facturacion', durable=True)
    channel.queue_bind(exchange='agroflow-exchange', queue='cola_facturacion', routing_key='nueva_cosecha')
    channel.basic_consume(queue='cola_facturacion', on_message_callback=callback)
    print('Esperando mensajes en cola_facturacion. Para salir, presiona Ctrl+C')
    channel.start_consuming()

if __name__ == '__main__':
    consumer_thread = threading.Thread(target=start_consumer)
    consumer_thread.start()