import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
// Importamos tus nuevas pantallas modulares
import Home from "./pages/Home";
import Vehicle from "./pages/Vehicle";
import Error404 from "./pages/Error404";
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      {/* BARRA DE NAVEGACIÓN FIJA */}
      <nav style={{ padding: "10px", background: "#eee", display: "flex", gap: "15px" }}>
        <Link to="/">Inicio</Link>
        <Link to="/vehicles">Gestión de Vehículos</Link>
        <Link to="/probar-error">Probar 404</Link>
      </nav>

      {/* CONTENIDO DINÁMICO */}
      <div style={{ marginTop: "20px" }}>
        <Routes>
          {/* Mapeamos las rutas directo a tus componentes de la carpeta pages */}
          <Route path="/" element={<Home nombre="Lucas" edad={25} activo={true} />} />
          <Route path="/vehicles" element={<Vehicle />} />
          
          {/* Comodín para atrapar cualquier otra URL */}
          <Route path="*" element={<Error404 />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}