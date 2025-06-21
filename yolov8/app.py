from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
from ultralytics import YOLO
import cv2
import numpy as np
import os
import tempfile
import base64
from PIL import Image
import io

app = Flask(__name__)

# Load your custom YOLO model
MODEL_PATH = 'my_model.pt'  # Path to your trained model
model = YOLO(MODEL_PATH)

# Configure upload settings
UPLOAD_FOLDER = 'temp_uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'bmp', 'tiff', 'webp'}

# Create upload folder if it doesn't exist
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def process_image(image_path):
    """Process image with YOLO model and return results"""
    try:
        # Run inference
        results = model(image_path)
        
        detections = []
        for result in results:
            boxes = result.boxes
            if boxes is not None:
                for box in boxes:
                    # Get box coordinates
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    
                    # Get confidence and class
                    confidence = float(box.conf[0])
                    class_id = int(box.cls[0])
                    class_name = model.names[class_id]
                    
                    detection = {
                        'class_name': class_name,
                        'class_id': class_id,
                        'confidence': confidence,
                        'bbox': {
                            'x1': x1,
                            'y1': y1,
                            'x2': x2,
                            'y2': y2,
                            'width': x2 - x1,
                            'height': y2 - y1
                        }
                    }
                    detections.append(detection)
        
        return {
            'status': 'success',
            'detections': detections,
            'detection_count': len(detections)
        }
    
    except Exception as e:
        return {
            'status': 'error',
            'message': str(e)
        }

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        'message': 'YOLOv8 Custom Model API',
        'status': 'active',
        'endpoints': {
            'predict': '/predict (POST)',
            'predict_base64': '/predict_base64 (POST)',
            'health': '/health (GET)'
        }
    })

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'healthy', 'model_loaded': MODEL_PATH})

@app.route('/predict', methods=['POST'])
def predict():
    """Predict endpoint for file upload"""
    try:
        # Check if file is in request
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        file = request.files['image']
        
        # Check if file is selected
        if file.filename == '':
            return jsonify({'error': 'No file selected'}), 400
        
        # Check file extension
        if not allowed_file(file.filename):
            return jsonify({'error': 'Invalid file format. Allowed: png, jpg, jpeg, gif, bmp, tiff, webp'}), 400
        
        # Save file temporarily
        filename = secure_filename(file.filename)
        temp_path = os.path.join(UPLOAD_FOLDER, filename)
        file.save(temp_path)
        
        # Process image
        result = process_image(temp_path)
        
        # Clean up temporary file
        os.remove(temp_path)
        
        return jsonify(result)
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/predict_base64', methods=['POST'])
def predict_base64():
    """Predict endpoint for base64 encoded images"""
    try:
        data = request.get_json()
        
        if not data or 'image' not in data:
            return jsonify({'error': 'No base64 image data provided'}), 400
        
        # Decode base64 image
        image_data = data['image']
        
        # Remove data URL prefix if present
        if image_data.startswith('data:image'):
            image_data = image_data.split(',')[1]
        
        # Decode base64
        image_bytes = base64.b64decode(image_data)
        
        # Convert to PIL Image
        image = Image.open(io.BytesIO(image_bytes))
        
        # Save temporarily
        temp_path = os.path.join(UPLOAD_FOLDER, 'temp_image.jpg')
        image.save(temp_path)
        
        # Process image
        result = process_image(temp_path)
        
        # Clean up
        os.remove(temp_path)
        
        return jsonify(result)
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/model_info', methods=['GET'])
def model_info():
    """Get information about the loaded model"""
    try:
        return jsonify({
            'model_path': MODEL_PATH,
            'class_names': model.names,
            'num_classes': len(model.names)
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    print(f"Loading model from: {MODEL_PATH}")
    print(f"Model classes: {model.names}")
    print("Starting Flask server...")
    app.run(host='0.0.0.0', port=5000, debug=False)