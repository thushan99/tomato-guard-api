FROM python:3.12.9-slim

WORKDIR /app

# Copy requirements file and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the model files first and check if they exist
COPY application_rate_model.pkl /app/
COPY herbicide_model.pkl /app/
RUN ls -la /app/

# Copy the API code
COPY app.py /app/

# Expose the port the app runs on
EXPOSE 5000

# Command to run the application
CMD ["python", "app.py"]