from flask import Flask, request, jsonify
import tensorflow as tf
from tensorflow.keras.models import load_model
import numpy as np
from PIL import Image
import io

app = Flask(__name__)
model = load_model('./tomato_weed_identify_model.h5')  # Load your model
class_labels = ['black_nightshade', 'cutleaf_nightshade', 'ground_cherry', 'hairy_nightshade', 'tomato']  # Replace with your classes

def preprocess_image(image_data):
    img = Image.open(image_data).convert('RGB')
    img = img.resize((224, 224))  # Resize to match model input
    img_array = np.array(img) / 255.0  # Normalize
    return np.expand_dims(img_array, axis=0)

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Check if file part is present
        if 'file' not in request.files:
            return jsonify({'error': 'No file part in the request'}), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({'error': 'No selected file'}), 400

        # Process the file
        input_image = preprocess_image(file)
        predictions = model.predict(input_image)[0]

        # Get the class with the highest confidence
        confidence = float(np.max(predictions))
        label = class_labels[np.argmax(predictions)]
        return jsonify({'weed_type': label, 'confidence': confidence})
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)