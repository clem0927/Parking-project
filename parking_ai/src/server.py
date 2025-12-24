# src/server.py
from flask import Flask
from flask_cors import CORS

from chatbot.routes import chat_bp
from predict.routes import predict_bp

app = Flask(__name__)
CORS(app, resources={r"/ml/*": {"origins": "*"}})

app.register_blueprint(chat_bp)
app.register_blueprint(predict_bp)

print(app.url_map)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
