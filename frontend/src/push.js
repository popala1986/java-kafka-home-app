import { deletePushSubscription, getPushPublicKey, savePushSubscription } from './api.js'

// Czy ta przeglądarka umie powiadomienia push?
// 'dev'         = npm run dev: Service Worker działa tylko w wersji zbudowanej (Caddy, https://localhost)
// 'unsupported' = brak obsługi, np. iPhone w zwykłym Safari (działa dopiero w apce z ekranu początkowego)
// 'ok'          = można włączać
export function pushSupport() {
  if (import.meta.env.DEV) {
    return 'dev'
  }
  if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
    return 'unsupported'
  }
  return 'ok'
}

// Subskrypcja tego urządzenia albo null, jeśli powiadomienia są wyłączone.
export async function currentSubscription() {
  const registration = await navigator.serviceWorker.ready
  return registration.pushManager.getSubscription()
}

export async function enablePush() {
  // Okienko przeglądarki „Zezwolić na powiadomienia?”. Pyta tylko raz; odmowę zmienia się w ustawieniach.
  const permission = await Notification.requestPermission()
  if (permission !== 'granted') {
    throw new Error('Powiadomienia są zablokowane. Zezwól na nie w ustawieniach przeglądarki (kłódka obok adresu).')
  }

  const { publicKey } = await getPushPublicKey()
  if (!publicKey) {
    throw new Error('Serwer nie ma kluczy VAPID, więc powiadomienia push są wyłączone.')
  }

  // Przeglądarka rejestruje się u swojego dostawcy push (Google, Apple, Mozilla) i dostaje adres (endpoint).
  const registration = await navigator.serviceWorker.ready
  const subscription = await registration.pushManager.subscribe({
    userVisibleOnly: true, // obietnica: każde powiadomienie pokażemy na ekranie, nic „po cichu”
    applicationServerKey: base64UrlToBytes(publicKey),
  })
  await savePushSubscription(subscription.toJSON())
}

export async function disablePush() {
  const subscription = await currentSubscription()
  if (subscription) {
    await deletePushSubscription(subscription.toJSON())
    await subscription.unsubscribe()
  }
}

// Klucz VAPID przychodzi jako tekst Base64 (wersja do adresów: - i _ zamiast + i /), a przeglądarka chce bajtów.
function base64UrlToBytes(text) {
  const base64 = text.replace(/-/g, '+').replace(/_/g, '/') + '='.repeat((4 - (text.length % 4)) % 4)
  return Uint8Array.from(atob(base64), (char) => char.charCodeAt(0))
}
