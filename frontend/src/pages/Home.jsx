export default function Home(props) {

  const { nombre, edad, activo } = props

  return (
    <div style={{ padding: '20px' }}>
      <h2>🏠 Bienvenidos al Panel de Control</h2>
      <p>Nombre: {nombre} | Edad: {edad} | Es Activo: {(activo) ? 'Si' : 'No'}.</p>
      <p>Este es el inicio de la gestión de flotas.</p>
    </div>
  );
}

