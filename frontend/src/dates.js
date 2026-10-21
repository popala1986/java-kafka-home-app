// Pomocnicze funkcje do dat. Backend zawsze dostaje i oddaje daty jako tekst "2026-10-26".

// Poniedziałek tygodnia, w którym jest podany dzień (jak previousOrSame(MONDAY) w Javie).
export function mondayOf(date) {
  const result = new Date(date)
  const daysSinceMonday = (result.getDay() + 6) % 7 // getDay(): 0 = niedziela, 1 = poniedziałek, ...
  result.setDate(result.getDate() - daysSinceMonday)
  return result
}

export function addDays(date, days) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

// Date -> "2026-10-26" (w czasie lokalnym, bez przesunięć strefy czasowej).
export function toIsoDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// "poniedziałek 26.10"
export function dayLabel(date) {
  const weekday = date.toLocaleDateString('pl-PL', { weekday: 'long' })
  const day = date.getDate()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${weekday} ${day}.${month}`
}
