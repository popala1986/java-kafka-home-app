import { useCallback, useEffect, useState } from 'react'
import { savePushSubscription, sendTestPush, UnauthorizedError } from './api.js'
import { currentSubscription, disablePush, enablePush, pushSupport } from './push.js'

// "Powiadomienia": każdy domownik sam włącza je na swoim telefonie (rodzic nie może tego zrobić za dziecko).
export default function NotificationsPage({ onUnauthorized }) {
  const support = pushSupport()
  const [enabled, setEnabled] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  const handleError = useCallback(
    (e) => (e instanceof UnauthorizedError ? onUnauthorized(e) : setError(e.message)),
    [onUnauthorized],
  )

  // Czy to urządzenie ma już włączone powiadomienia? Jeśli tak, wysyłamy je jeszcze raz do backendu:
  // na tym samym telefonie mógł się wcześniej logować ktoś inny, a powiadomienia mają iść do Ciebie.
  useEffect(() => {
    if (support !== 'ok') {
      return
    }
    currentSubscription()
      .then((subscription) => {
        setEnabled(subscription !== null)
        return subscription && savePushSubscription(subscription.toJSON())
      })
      .catch(handleError)
  }, [support, handleError])

  async function run(action, message) {
    setBusy(true)
    setError(null)
    setSuccess(null)
    try {
      await action()
      setSuccess(message)
    } catch (e) {
      handleError(e)
    } finally {
      setBusy(false)
    }
  }

  function handleEnable() {
    run(async () => {
      await enablePush()
      setEnabled(true)
    }, 'Powiadomienia włączone na tym urządzeniu.')
  }

  function handleDisable() {
    run(async () => {
      await disablePush()
      setEnabled(false)
    }, 'Powiadomienia wyłączone na tym urządzeniu.')
  }

  async function handleTest() {
    setBusy(true)
    setError(null)
    setSuccess(null)
    try {
      const { delivered } = await sendTestPush()
      if (delivered > 0) {
        setSuccess(`Wysłano na urządzenia: ${delivered}. Powiadomienie pojawi się za kilka sekund.`)
      } else {
        setError('Nie dotarło na żadne urządzenie. Wyłącz i włącz powiadomienia jeszcze raz.')
      }
    } catch (e) {
      handleError(e)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      <section className="card">
        <h2>Powiadomienia na tym urządzeniu</h2>

        {support === 'dev' && (
          <p className="muted">Powiadomienia działają w wersji zbudowanej: npm run build i https://localhost.</p>
        )}
        {support === 'unsupported' && (
          <p className="muted">
            Ta przeglądarka nie obsługuje powiadomień. Na iPhonie najpierw dodaj apkę do ekranu początkowego
            (Udostępnij → Do ekranu początkowego) i otwórz ją z ikonki.
          </p>
        )}

        {support === 'ok' && (
          <>
            <p className="muted">
              {enabled
                ? 'Włączone. Rano dostaniesz listę swoich zadań na dziś.'
                : 'Wyłączone. Włącz, żeby rano dostawać listę swoich zadań na dziś. Bez tego przyjdzie e-mail.'}
            </p>
            {enabled ? (
              <>
                <button className="full" onClick={handleTest} disabled={busy}>Wyślij próbne powiadomienie</button>
                <button className="full secondary" onClick={handleDisable} disabled={busy}>Wyłącz powiadomienia</button>
              </>
            ) : (
              <button className="full" onClick={handleEnable} disabled={busy}>Włącz powiadomienia</button>
            )}
          </>
        )}
      </section>
    </>
  )
}
