#!/usr/bin/env python3
"""
AmazeCare Hospital Management System - Flask Application
Author: Sushmitha S
Project: Hospital Management System with Python Flask
"""

from flask import Flask, render_template, request, jsonify, redirect, url_for, flash, session
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime
import os

# Initialize Flask app
app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key-here'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///amazecare.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Initialize extensions
db = SQLAlchemy(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

# User loader for Flask-Login
@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

# Database Models
class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(120), nullable=False)
    role = db.Column(db.String(20), nullable=False, default='patient')  # admin, doctor, nurse, patient
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Patient(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    date_of_birth = db.Column(db.Date, nullable=False)
    gender = db.Column(db.String(10), nullable=False)
    phone = db.Column(db.String(15), nullable=False)
    address = db.Column(db.Text, nullable=False)
    emergency_contact = db.Column(db.String(15), nullable=False)
    medical_history = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Doctor(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    specialization = db.Column(db.String(100), nullable=False)
    license_number = db.Column(db.String(50), unique=True, nullable=False)
    phone = db.Column(db.String(15), nullable=False)
    department = db.Column(db.String(50), nullable=False)
    experience_years = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Appointment(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    patient_id = db.Column(db.Integer, db.ForeignKey('patient.id'), nullable=False)
    doctor_id = db.Column(db.Integer, db.ForeignKey('doctor.id'), nullable=False)
    appointment_date = db.Column(db.DateTime, nullable=False)
    reason = db.Column(db.String(200), nullable=False)
    status = db.Column(db.String(20), nullable=False, default='scheduled')  # scheduled, completed, cancelled
    notes = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

# Routes
@app.route('/')
def index():
    """Home page"""
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    """User login"""
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        
        user = User.query.filter_by(username=username).first()
        
        if user and check_password_hash(user.password_hash, password):
            login_user(user)
            flash('Login successful!', 'success')
            return redirect(url_for('dashboard'))
        else:
            flash('Invalid username or password!', 'error')
    
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    """User registration"""
    if request.method == 'POST':
        username = request.form['username']
        email = request.form['email']
        password = request.form['password']
        role = request.form.get('role', 'patient')
        
        # Check if user already exists
        if User.query.filter_by(username=username).first():
            flash('Username already exists!', 'error')
            return render_template('register.html')
        
        if User.query.filter_by(email=email).first():
            flash('Email already registered!', 'error')
            return render_template('register.html')
        
        # Create new user
        user = User(
            username=username,
            email=email,
            password_hash=generate_password_hash(password),
            role=role
        )
        
        db.session.add(user)
        db.session.commit()
        
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('login'))
    
    return render_template('register.html')

@app.route('/logout')
@login_required
def logout():
    """User logout"""
    logout_user()
    flash('You have been logged out.', 'info')
    return redirect(url_for('index'))

@app.route('/dashboard')
@login_required
def dashboard():
    """User dashboard"""
    if current_user.role == 'admin':
        return render_template('admin_dashboard.html')
    elif current_user.role == 'doctor':
        return render_template('doctor_dashboard.html')
    elif current_user.role == 'nurse':
        return render_template('nurse_dashboard.html')
    else:  # patient
        return render_template('patient_dashboard.html')

@app.route('/patients')
@login_required
def patients():
    """List all patients (admin/doctor/nurse only)"""
    if current_user.role not in ['admin', 'doctor', 'nurse']:
        flash('Access denied!', 'error')
        return redirect(url_for('dashboard'))
    
    patients = Patient.query.all()
    return render_template('patients.html', patients=patients)

@app.route('/doctors')
@login_required
def doctors():
    """List all doctors"""
    doctors = Doctor.query.all()
    return render_template('doctors.html', doctors=doctors)

@app.route('/appointments')
@login_required
def appointments():
    """List appointments"""
    if current_user.role == 'patient':
        # Show only patient's appointments
        patient = Patient.query.filter_by(user_id=current_user.id).first()
        if patient:
            appointments = Appointment.query.filter_by(patient_id=patient.id).all()
        else:
            appointments = []
    elif current_user.role == 'doctor':
        # Show only doctor's appointments
        doctor = Doctor.query.filter_by(user_id=current_user.id).first()
        if doctor:
            appointments = Appointment.query.filter_by(doctor_id=doctor.id).all()
        else:
            appointments = []
    else:
        # Admin/nurse can see all appointments
        appointments = Appointment.query.all()
    
    return render_template('appointments.html', appointments=appointments)

@app.route('/book_appointment', methods=['GET', 'POST'])
@login_required
def book_appointment():
    """Book a new appointment"""
    if request.method == 'POST':
        doctor_id = request.form['doctor_id']
        appointment_date = datetime.strptime(request.form['appointment_date'], '%Y-%m-%dT%H:%M')
        reason = request.form['reason']
        
        # Get patient info
        patient = Patient.query.filter_by(user_id=current_user.id).first()
        if not patient:
            flash('Please complete your patient profile first!', 'error')
            return redirect(url_for('profile'))
        
        # Create appointment
        appointment = Appointment(
            patient_id=patient.id,
            doctor_id=doctor_id,
            appointment_date=appointment_date,
            reason=reason
        )
        
        db.session.add(appointment)
        db.session.commit()
        
        flash('Appointment booked successfully!', 'success')
        return redirect(url_for('appointments'))
    
    doctors = Doctor.query.all()
    return render_template('book_appointment.html', doctors=doctors)

@app.route('/profile')
@login_required
def profile():
    """User profile"""
    if current_user.role == 'patient':
        patient = Patient.query.filter_by(user_id=current_user.id).first()
        return render_template('patient_profile.html', patient=patient)
    elif current_user.role == 'doctor':
        doctor = Doctor.query.filter_by(user_id=current_user.id).first()
        return render_template('doctor_profile.html', doctor=doctor)
    else:
        return render_template('profile.html')

# API Routes
@app.route('/api/patients', methods=['GET'])
@login_required
def api_patients():
    """API endpoint to get patients data"""
    if current_user.role not in ['admin', 'doctor', 'nurse']:
        return jsonify({'error': 'Access denied'}), 403
    
    patients = Patient.query.all()
    return jsonify([{
        'id': p.id,
        'name': f"{p.first_name} {p.last_name}",
        'phone': p.phone,
        'gender': p.gender,
        'created_at': p.created_at.isoformat()
    } for p in patients])

@app.route('/api/appointments', methods=['GET'])
@login_required
def api_appointments():
    """API endpoint to get appointments data"""
    appointments = Appointment.query.all()
    return jsonify([{
        'id': a.id,
        'patient_id': a.patient_id,
        'doctor_id': a.doctor_id,
        'date': a.appointment_date.isoformat(),
        'reason': a.reason,
        'status': a.status
    } for a in appointments])

# Initialize database
def init_db():
    """Initialize database with tables"""
    with app.app_context():
        db.create_all()
        
        # Create admin user if it doesn't exist
        admin = User.query.filter_by(username='admin').first()
        if not admin:
            admin = User(
                username='admin',
                email='admin@amazecare.com',
                password_hash=generate_password_hash('admin123'),
                role='admin'
            )
            db.session.add(admin)
            db.session.commit()
            print("Admin user created: username=admin, password=admin123")

if __name__ == '__main__':
    init_db()
    app.run(debug=True, host='0.0.0.0', port=5000)