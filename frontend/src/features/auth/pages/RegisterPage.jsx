import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const Register = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    rol: 'usuario'
  });
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (error) setError('');
  };

  const validateForm = () => {
    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return false;
    }
    
    if (formData.password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return false;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Ingrese un email válido');
      return false;
    }
    
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setError('El nombre y apellido son obligatorios');
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setLoading(true);
    setError('');

    try {
      const response = await axios.post('http://localhost:3000/api/v1/auth/register', {
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
        rol: formData.rol
      });

      if (response.data.success) {
        navigate('/login', { 
          state: { message: 'Registro exitoso. Por favor inicia sesión.' }
        });
      }
      
    } catch (err) {
      if (err.response) {
        setError(err.response.data.message || 'Error al registrar usuario');
      } else if (err.request) {
        setError('No se pudo conectar con el servidor');
      } else {
        setError('Error al procesar la solicitud');
      }
      console.error('Error en registro:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h4 className="title">Registrarse</h4>
      
      {error && (
        <div className="p-3 mb-4 text-sm text-red-700 bg-red-100 rounded-lg">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="field">
          <svg className="input-icon" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <path d="M207.8 20.73c-93.45 18.32-168.7 93.66-187 187.1c-27.64 140.9 68.65 266.2 199.1 285.1c19.01 2.888 36.17-12.26 36.17-31.49l.0001-.6631c0-15.74-11.44-28.88-26.84-31.24c-84.35-12.98-149.2-86.13-149.2-174.2c0-102.9 88.61-185.5 193.4-175.4c91.54 8.869 158.6 91.25 158.6 183.2l0 16.16c0 22.09-17.94 40.05-40 40.05s-40.01-17.96-40.01-40.05v-120.1c0-8.847-7.161-16.02-16.01-16.02l-31.98 .0036c-7.299 0-13.2 4.992-15.12 11.68c-24.85-12.15-54.24-16.38-86.06-5.106c-38.75 13.73-68.12 48.91-73.72 89.64c-9.483 69.01 43.81 128 110.9 128c26.44 0 50.43-9.544 69.59-24.88c24 31.3 65.23 48.69 109.4 37.49C465.2 369.3 496 324.1 495.1 277.2V256.3C495.1 107.1 361.2-9.332 207.8 20.73zM239.1 304.3c-26.47 0-48-21.56-48-48.05s21.53-48.05 48-48.05s48 21.56 48 48.05S266.5 304.3 239.1 304.3z"></path>
          </svg>
          <input 
            autoComplete="off" 
            id="regemail" 
            placeholder="Email" 
            className="input-field" 
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="field">
          <svg className="input-icon" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 64C0 28.65 28.65 0 64 0H448C483.3 0 512 28.65 512 64V288C512 323.3 483.3 352 448 352H64C28.65 352 0 323.3 0 288V64zM64 96V128H448V96H64zM64 192V224H448V192H64z" />
          </svg>
          <input 
            autoComplete="off" 
            id="regfirstname" 
            placeholder="Nombre" 
            className="input-field" 
            name="firstName"
            type="text"
            value={formData.firstName}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="field">
          <svg className="input-icon" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 64C0 28.65 28.65 0 64 0H448C483.3 0 512 28.65 512 64V288C512 323.3 483.3 352 448 352H64C28.65 352 0 323.3 0 288V64zM64 96V128H448V96H64zM64 192V224H448V192H64z" />
          </svg>
          <input 
            autoComplete="off" 
            id="reglastname" 
            placeholder="Apellido" 
            className="input-field" 
            name="lastName"
            type="text"
            value={formData.lastName}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>
        
        <div className="field">
          <svg className="input-icon" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <path d="M80 192V144C80 64.47 144.5 0 224 0C303.5 0 368 64.47 368 144V192H384C419.3 192 448 220.7 448 256V448C448 483.3 419.3 512 384 512H64C28.65 512 0 483.3 0 448V256C0 220.7 28.65 192 64 192H80zM144 192H304V144C304 99.82 268.2 64 224 64C179.8 64 144 99.82 144 144V192z"></path>
          </svg>
          <input 
            autoComplete="off" 
            id="regpassword" 
            placeholder="Contraseña" 
            className="input-field" 
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <div className="field">
          <svg className="input-icon" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <path d="M80 192V144C80 64.47 144.5 0 224 0C303.5 0 368 64.47 368 144V192H384C419.3 192 448 220.7 448 256V448C448 483.3 419.3 512 384 512H64C28.65 512 0 483.3 0 448V256C0 220.7 28.65 192 64 192H80zM144 192H304V144C304 99.82 268.2 64 224 64C179.8 64 144 99.82 144 144V192z"></path>
          </svg>
          <input 
            autoComplete="off" 
            id="regconfirmpassword" 
            placeholder="Confirmar Contraseña" 
            className="input-field" 
            name="confirmPassword"
            type="password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            disabled={loading}
          />
        </div>

        <button className="btn" type="submit" disabled={loading}>
          {loading ? 'Registrando...' : 'Registrarse'}
        </button>
      </form>

      <div className="mt-4 text-center">
        <span className="text-sm">¿Ya tienes cuenta? </span>
        <Link to="/login" className="text-sm font-semibold hover:underline">
          Inicia sesión aquí
        </Link>
      </div>
    </div>
  );
};

export default Register;