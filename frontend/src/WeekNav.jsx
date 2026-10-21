import { addDays, dayLabel } from './dates.js'

// Strzałki tygodnia. Ten sam kawałek ekranu jest w "Mój tydzień" i w "Plan rodziny",
// więc wydzielamy go do osobnego komponentu (jak wspólną metodę w Javie).
export default function WeekNav({ weekStart, onChange }) {
  return (
    <nav className="week-nav">
      <button onClick={() => onChange(addDays(weekStart, -7))} aria-label="Poprzedni tydzień">‹</button>
      <strong>Tydzień od {dayLabel(weekStart)}</strong>
      <button onClick={() => onChange(addDays(weekStart, 7))} aria-label="Następny tydzień">›</button>
    </nav>
  )
}
