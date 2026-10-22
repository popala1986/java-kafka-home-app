// Service Worker: osobny skrypt, który przeglądarka trzyma obok strony i uruchamia w tle.
// Widzi każde zapytanie strony (zdarzenie "fetch") i może odpowiedzieć z własnej pamięci (Cache).
// W Kroku 13c ten sam plik odbierze powiadomienia push.

// Zmień numer, gdy zmienisz ten plik: stara pamięć zostanie wtedy wyczyszczona.
const CACHE = 'homeapp-v2'

// Szkielet aplikacji: zapisujemy go od razu przy instalacji, żeby działał bez sieci już po pierwszej wizycie.
const SHELL = ['/', '/manifest.webmanifest', '/icons/icon-192.png']

// Nowa wersja Service Workera od razu przejmuje stronę, bez czekania na zamknięcie wszystkich kart.
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE)
      .then(async (cache) => {
        await cache.addAll(SHELL)
        // index.html wskazuje pliki JS i CSS z hashem w nazwie (np. /assets/index-B8fw3k.js). Zapisz też je.
        const html = await (await cache.match('/')).text()
        const assets = [...html.matchAll(/\/assets\/[^"']+/g)].map((match) => match[0])
        await cache.addAll(assets)
      })
      .then(() => self.skipWaiting()),
  )
})

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE).map((key) => caches.delete(key))))
      .then(() => self.clients.claim()),
  )
})

self.addEventListener('fetch', (event) => {
  const request = event.request
  const url = new URL(request.url)

  // API zostawiamy w spokoju: plan i domownicy mają być zawsze świeże, prosto z backendu.
  if (request.method !== 'GET' || url.origin !== self.location.origin || url.pathname.startsWith('/api/')) {
    return
  }

  // Pliki aplikacji: najpierw sieć (zawsze najnowsza wersja), a bez sieci ostatnia zapisana kopia.
  event.respondWith(
    fetch(request)
      .then((response) => {
        if (response.ok) {
          const copy = response.clone()
          caches.open(CACHE).then((cache) => cache.put(request, copy))
        }
        return response
      })
      .catch(() => caches.match(request).then((cached) => cached ?? caches.match('/'))),
  )
})
