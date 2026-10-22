import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import './styles.css'

// Punkt startowy: React "przejmuje" <div id="root"> z index.html i rysuje w nim komponent App.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
// Service Worker rejestrujemy tylko w wersji zbudowanej (npm run build, serwowanej przez Caddy).
// W npm run dev by przeszkadzał: podsuwałby stare pliki z pamięci zamiast świeżych.
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .catch((error) => console.error('Nie udało się zarejestrować Service Workera', error))
  })
}
// Service Worker rejestrujemy tylko w wersji zbudowanej (npm run build, serwowanej przez Caddy).
// W npm run dev by przeszkadzał: podsuwałby stare pliki z pamięci zamiast świeżych.
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .catch((error) => console.error('Nie udało się zarejestrować Service Workera', error))
  })
}
