-- Katalog startowych obowiązków (50 pozycji z pliku 01-lista-obowiazkow.md).
-- To jest WZÓR wspólny dla wszystkich rodzin. Rodziny dostają własną kopię (V3),
-- więc gdy admin coś zmieni u siebie, wzór i inne rodziny zostają nietknięte.
CREATE TABLE chore_templates (
    code             VARCHAR(10)  PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    frequency        VARCHAR(30)  NOT NULL,
    min_age          INT          NOT NULL,
    duration_minutes INT          NOT NULL,
    points           INT          NOT NULL
);

INSERT INTO chore_templates (code, name, type, frequency, min_age, duration_minutes, points) VALUES
-- Grupa od 4 lat
('K4-01', 'Pościel swoje łóżko (narzuta, poduszka)', 'PERSONAL', 'DAILY', 4, 3, 1),
('K4-02', 'Odłóż zabawki na miejsce przed snem', 'PERSONAL', 'DAILY', 4, 10, 1),
('K4-03', 'Odnieś swój talerz i kubek do kuchni po posiłku', 'PERSONAL', 'DAILY', 4, 2, 1),
('K4-04', 'Wrzuć brudne ubrania do kosza na pranie', 'PERSONAL', 'DAILY', 4, 2, 1),
('K4-05', 'Odłóż buty i kurtkę na miejsce po powrocie', 'PERSONAL', 'DAILY', 4, 2, 1),
('K4-06', 'Podlej kwiatek (wskazany przez rodzica)', 'SHARED', 'THREE_TIMES_A_WEEK', 4, 3, 1),
('K4-07', 'Pomóż nakryć do stołu (serwetki, sztućce)', 'SHARED', 'DAILY', 4, 5, 1),
('K4-08', 'Nakarm zwierzaka (z odmierzoną porcją)', 'SHARED', 'DAILY', 4, 3, 1),
('K4-09', 'Dopasuj skarpetki w parę po praniu', 'SHARED', 'WEEKLY', 4, 10, 1),
('K4-10', 'Zetrzyj mokrą ściereczką swój stolik/blat', 'PERSONAL', 'WEEKLY', 4, 5, 1),
-- Grupa od 8 lat
('K8-01', 'Spakuj plecak na następny dzień', 'PERSONAL', 'DAILY', 8, 5, 1),
('K8-02', 'Posprzątaj swoje biurko', 'PERSONAL', 'THREE_TIMES_A_WEEK', 8, 10, 1),
('K8-03', 'Nakryj do stołu i sprzątnij po posiłku', 'SHARED', 'DAILY', 8, 10, 2),
('K8-04', 'Wyjmij czyste naczynia ze zmywarki', 'SHARED', 'DAILY', 8, 10, 1),
('K8-05', 'Wynieś śmieci segregowane (plastik/papier)', 'SHARED', 'THREE_TIMES_A_WEEK', 8, 5, 1),
('K8-06', 'Odkurz swój pokój', 'PERSONAL', 'WEEKLY', 8, 15, 2),
('K8-07', 'Złóż i odłóż swoje czyste ubrania', 'PERSONAL', 'WEEKLY', 8, 15, 1),
('K8-08', 'Zetrzyj kurze w salonie', 'SHARED', 'WEEKLY', 8, 15, 2),
('K8-09', 'Rozpakuj zakupy i odłóż na miejsce', 'SHARED', 'WEEKLY', 8, 10, 1),
('K8-10', 'Wyczyść kuwetę / klatkę zwierzaka (z pomocą)', 'SHARED', 'WEEKLY', 8, 15, 2),
-- Grupa od 11 lat
('K11-01', 'Zmień pościel w swoim łóżku', 'PERSONAL', 'WEEKLY', 11, 15, 2),
('K11-02', 'Załaduj i włącz zmywarkę', 'SHARED', 'DAILY', 11, 10, 1),
('K11-03', 'Wynieś wszystkie śmieci (w tym zmieszane)', 'SHARED', 'THREE_TIMES_A_WEEK', 11, 5, 1),
('K11-04', 'Umyj podłogę w kuchni', 'SHARED', 'WEEKLY', 11, 20, 2),
('K11-05', 'Wyczyść umywalkę i lustro w łazience', 'SHARED', 'WEEKLY', 11, 15, 2),
('K11-06', 'Rozwieś pranie / zdejmij suche pranie', 'SHARED', 'THREE_TIMES_A_WEEK', 11, 15, 2),
('K11-07', 'Przygotuj proste śniadanie lub kanapki dla siebie', 'PERSONAL', 'DAILY', 11, 10, 1),
('K11-08', 'Wyprowadź psa na spacer', 'SHARED', 'DAILY', 11, 20, 2),
('K11-09', 'Odkurz salon i przedpokój', 'SHARED', 'WEEKLY', 11, 20, 2),
('K11-10', 'Pomóż w ogrodzie / zamieć balkon', 'SHARED', 'WEEKLY', 11, 20, 2),
-- Grupa od 14 lat
('K14-01', 'Nastaw pranie (posortuj kolory, dobierz program)', 'SHARED', 'THREE_TIMES_A_WEEK', 14, 15, 2),
('K14-02', 'Wyprasuj swoje ubrania', 'PERSONAL', 'WEEKLY', 14, 20, 2),
('K14-03', 'Ugotuj prosty obiad dla rodziny', 'SHARED', 'WEEKLY', 14, 60, 3),
('K14-04', 'Posprzątaj całą łazienkę (wanna, WC, podłoga)', 'SHARED', 'WEEKLY', 14, 40, 3),
('K14-05', 'Zrób zakupy wg listy', 'SHARED', 'WEEKLY', 14, 45, 2),
('K14-06', 'Wyczyść kuchenkę i blaty', 'SHARED', 'WEEKLY', 14, 20, 2),
('K14-07', 'Umyj podłogi w całym mieszkaniu', 'SHARED', 'WEEKLY', 14, 40, 3),
('K14-08', 'Wyczyść lodówkę (wyrzuć przeterminowane)', 'SHARED', 'MONTHLY', 14, 30, 2),
('K14-09', 'Skoś trawnik / odśnież wejście (sezonowo)', 'SHARED', 'WEEKLY', 14, 45, 3),
('K14-10', 'Pomóż młodszemu rodzeństwu w lekcjach', 'SHARED', 'THREE_TIMES_A_WEEK', 14, 30, 2),
-- Grupa od 18 lat
('K18-01', 'Zaplanuj jadłospis i listę zakupów na tydzień', 'SHARED', 'WEEKLY', 18, 30, 2),
('K18-02', 'Ugotuj obiad', 'SHARED', 'DAILY', 18, 60, 3),
('K18-03', 'Opłać rachunki i sprawdź budżet domowy', 'SHARED', 'MONTHLY', 18, 30, 2),
('K18-04', 'Umyj okna', 'SHARED', 'MONTHLY', 18, 90, 3),
('K18-05', 'Wyczyść piekarnik i mikrofalę', 'SHARED', 'MONTHLY', 18, 40, 3),
('K18-06', 'Rozmroź/umyj zamrażarkę, odkamień czajnik i ekspres', 'SHARED', 'MONTHLY', 18, 40, 2),
('K18-07', 'Wymień ręczniki i pościel w całym domu', 'SHARED', 'WEEKLY', 18, 30, 2),
('K18-08', 'Drobne naprawy i przegląd (żarówki, baterie w czujnikach)', 'SHARED', 'MONTHLY', 18, 30, 2),
('K18-09', 'Wizyty i sprawy urzędowe (lekarz, szkoła, urząd)', 'SHARED', 'MONTHLY', 18, 60, 2),
('K18-10', 'Gruntowne sprzątanie jednego pomieszczenia (rotacyjnie)', 'SHARED', 'WEEKLY', 18, 60, 3);
