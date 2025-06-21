# app.py
import pandas as pd
import numpy as np
import joblib
import os
import sys
from flask import Flask, request, jsonify

app = Flask(__name__)

# Set up logging
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global variables for models
reg_pipeline = None
clf_pipeline = None

# Load the trained models
def load_models():
    global reg_pipeline, clf_pipeline
    
    logger.info("Checking for model files...")
    for file in ["application_rate_model.pkl", "herbicide_model.pkl"]:
        if os.path.exists(file):
            logger.info(f"Found {file}")
        else:
            logger.error(f"Error: {file} not found!")
            logger.error(f"Current directory contents: {os.listdir('.')}")
            return False
    
    try:
        logger.info("Loading regression model...")
        reg_pipeline = joblib.load("application_rate_model.pkl")
        logger.info("Loading classification model...")
        clf_pipeline = joblib.load("herbicide_model.pkl")
        logger.info("Models loaded successfully!")
        return True
    except Exception as e:
        logger.error(f"Error loading models: {e}")
        return False

# Load models when app starts
models_loaded = load_models()

@app.route('/', methods=['GET'])
def home():
    return "Herbicide Prediction API is running! Send POST requests to /predict"

@app.route('/predict', methods=['POST'])
def predict():
    logger.info("Received prediction request")
    
    # Check if models are loaded
    global reg_pipeline, clf_pipeline
    if reg_pipeline is None or clf_pipeline is None:
        if not load_models():
            return jsonify({"error": "Failed to load models. Please check server logs."}), 500
    
    try:
        # Get JSON data from request
        data = request.json
        logger.info(f"Request data: {data}")
        
        # Convert to DataFrame
        input_data = pd.DataFrame({
            "Weed Name": [data["Weed Name"]],
            "Soil Type": [data["Soil Type"]],
            "Growth Stage": [data["Growth Stage"]],
            "Temp (°C)": [float(data["Temp (°C)"])],
            "Humidity (%)": [float(data["Humidity (%)"])],
            "Wind Speed (km/h)": [float(data["Wind Speed (km/h)"])],
            "Rainfall (mm)": [float(data["Rainfall (mm)"])]
        })
        
        logger.info("Making predictions...")
        # Make predictions
        reg_prediction = reg_pipeline.predict(input_data)[0]
        clf_prediction = clf_pipeline.predict(input_data)[0]
        
        # Create response
        response = {
            "Predicted Herbicide Name": str(clf_prediction),
            "Predicted Application Rate (L/ha)": round(float(reg_prediction), 2)
        }
        
        logger.info(f"Prediction response: {response}")
        return jsonify(response)
    
    except Exception as e:
        logger.error(f"Error during prediction: {e}", exc_info=True)
        return jsonify({"error": str(e)}), 400

if __name__ == '__main__':
    logger.info("Starting Herbicide Prediction API on port 5000...")
    app.run(host='0.0.0.0', port=5000, debug=False)