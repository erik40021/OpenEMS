export const TRANSLATION = {
    General: {
        Mode: "Modus",
        Automatic: "Automatisch",
        State: "Zustand",
        On: "An",
        Off: "Aus",
        Active: "Aktiv",
        Inactive: "Inaktiv",
        Manually: "Manuell",
        Phase: "Phase",
        Phases: "Phasen",
        Autarchy: "Autarkie",
        SelfConsumption: "Eigenverbrauch",
        Cumulative: "Kumulierte Werte",
        Grid: "Netz",
        GridBuy: "Netzbezug",
        GridSell: "Netzeinspeisung",
        GridBuyAdvanced: "Bezug",
        GridSellAdvanced: "Einspeisung",
        OffGrid: "Keine Netzverbindung!",
        Production: "Erzeugung",
        Consumption: "Verbrauch",
        otherConsumption: "Sonstiger",
        Total: "Gesamt",
        Load: "Last",
        Power: "Leistung",
        StorageSystem: "Speichersystem",
        History: "Historie",
        Live: 'Live',
        NoValue: "Kein Wert",
        Soc: "Ladezustand",
        Percentage: "Prozent",
        More: "Mehr...",
        ChargePower: "Beladung",
        DischargePower: "Entladung",
        ChargeDischarge: "Be-/Entladung",
        ActualPower: "E-Auto Beladung",
        PeriodFromTo: "{{value1}} - {{value2}}", // value1 = start date, value2 = end date
        DateFormat: "dd.MM.yyyy", // z.B. Englisch: yyyy-MM-dd (dd = Tag, MM = Monat, yyyy = Jahr)
        DateFormatShort: "dd.MM",
        Search: "Suchen",
        ChangeAccepted: "Änderung übernommen",
        ChangeFailed: "Änderung fehlgeschlagen",
        Week: {
            Monday: "Montag",
            Tuesday: "Dienstag",
            Wednesday: "Mittwoch",
            Thursday: "Donnerstag",
            Friday: "Freitag",
            Saturday: "Samstag",
            Sunday: "Sonntag"
        },
        Month: {
            January: "Januar",
            February: "Februar",
            March: "März",
            April: "April",
            May: "Mai",
            June: "Juni",
            July: "Juli",
            August: "August",
            September: "September",
            October: "Oktober",
            November: "November",
            December: "Dezember"
        },
        ReportValue: "Fehlerhafte Daten melden",
        Capacity: "Kapazität"
    },
    Menu: {
        Index: "Übersicht",
        AboutUI: "Über OpenEMS UI",
        GeneralSettings: 'Allgemeine Einstellungen',
        EdgeSettings: 'FEMS Einstellungen',
        Menu: 'Menü',
        Overview: 'FEMS Übersicht',
        Logout: 'Abmelden'
    },
    Index: {
        AllConnected: "Alle Verbindungen hergestellt.",
        ConnectionSuccessful: "Verbindung zu {{value}} hergestellt.", // value = name of websocket
        ConnectionFailed: "Verbindung zu {{value}} getrennt.", // value = name of websocket
        ToEnergymonitor: "Zum Energiemonitor...",
        IsOffline: "OpenEMS ist offline!"
    },
    Edge: {
        Index: {
            Energymonitor: {
                Title: "Energiemonitor",
                ConsumptionWarning: "Verbrauch & unbekannte Erzeuger",
                Storage: "Speicher",
                ReactivePower: "Blindleistung",
                ActivePower: "Ausgabeleistung",
                GridMeter: "Netzzähler",
                ProductionMeter: "Erzeugungszähler",
                StorageDischarge: "Speicher-Entladung",
                StorageCharge: "Speicher-Beladung"
            },
            Energytable: {
                Title: "Energietabelle",
                LoadingDC: "Beladung DC",
                ProductionDC: "Erzeugung DC"
            },
            Widgets: {
                Channeltreshold: {
                    Output: "Ausgang"
                },
                phasesInfo: "Die Summe der einzelnen Phasen kann aus technischen Gründen geringfügig von der Gesamtsumme abweichen.",
                autarchyInfo: "Die Autarkie gibt an zu wie viel Prozent die aktuell genutzte Leistung durch Erzeugung und Speicherentladung gedeckt wird.",
                selfconsumptionInfo: "Der Eigenverbrauch gibt an zu wie viel Prozent die aktuell erzeugte Leistung durch direkten Verbrauch und durch Speicherbeladung selbst genutzt wird.",
                twoWayInfoStorage: "Negative Werte entsprechen Speicher Beladung, Positive Werte entsprechen Speicher Entladung",
                twoWayInfoGrid: "Negative Werte entsprechen Netzeinspeisung, Positive Werte entsprechen Netzbezug",
                CHP: {
                    LowThreshold: "Unterer Schwellenwert",
                    HighThreshold: "Oberer Schwellenwert"
                },
                EVCS: {
                    ChargingStation: "Ladestation",
                    ChargingStationCluster: "Ladestation Cluster",
                    OverviewChargingStations: "Übersicht Ladestationen",
                    ChargingStationDeactivated: "Ladestation deaktiviert",
                    Prioritization: "Priorisierung",
                    Status: "Status",
                    Starting: "Startet",
                    NotReadyForCharging: "Nicht bereit zur Beladung",
                    ReadyForCharging: "Bereit zur Beladung",
                    Charging: "Beladung läuft",
                    NotCharging: "Keine Beladung",
                    Error: "Fehler",
                    NotAuthorized: "Nicht authorisiert",
                    Unplugged: "Ausgesteckt",
                    ChargeLimitReached: "Ladelimit erreicht",
                    ChargingStationPluggedIn: "Ladestation eingesteckt",
                    ChargingStationPluggedInLocked: "Ladestation eingesteckt + gesperrt",
                    ChargingStationPluggedInEV: "Ladestation + E-Auto eingesteckt",
                    ChargingStationPluggedInEVLocked: "Ladestation + E-Auto eingesteckt + gesperrt",
                    ChargingLimit: "Lade-Begrenzung",
                    AmountOfChargingStations: "Anzahl der Ladestationen",
                    ChargingPower: "Ladeleistung",
                    TotalChargingPower: "Gesamte Lade-Leistung",
                    CurrentCharge: "Aktuelle Beladung",
                    TotalCharge: "Gesamte Beladung",
                    EnforceCharging: "Erzwinge Beladung",
                    Cable: "Kabel",
                    CableNotConnected: "Kabel ist nicht angeschlossen",
                    CarFull: "Auto ist voll",
                    EnergieSinceBeginning: "Energie seit Ladebeginn",
                    ActivateCharging: "Aktivieren der Ladesäule",
                    ClusterConfigError: "Bei der Konfiguration des Evcs-Clusters ist ein Fehler aufgetreten",
                    EnergyLimit: "Energielimit",
                    MaxEnergyRestriction: "Maximale Energie pro Ladevorgang begrenzen",
                    NoConnection: {
                        Description: "Es konnte keine Verbindung zur Ladestation aufgebaut werden.",
                        Help1: "Prüfen sie ob die Ladestation eingeschaltet und über das Netz erreichbar ist",
                        Help1_1: "Die IP der Ladesäule erscheint beim erneuten einschalten"
                    },
                    OptimizedChargeMode: {
                        Name: "Optimierte Beladung",
                        ShortName: "Automatisch",
                        Info: "In diesem Modus wird die Beladung des Autos an die aktuelle Produktion und den aktuellen Verbrauch angepasst.",
                        MinInfo: "Falls verhindert werden soll, dass das Auto in der Nacht gar nicht lädt, kann eine minimale Aufladung festgelegt werden.",
                        MinCharging: "Minimale Beladung garantieren",
                        MinChargePower: "Minimale Ladestärke",
                        ChargingPriority: {
                            Info: "Je nach Priorisierung wird die ausgewählte Komponente zuerst beladen",
                            Car: "E-Auto",
                            Storage: "Speicher"
                        }
                    },
                    ForceChargeMode: {
                        Name: "Erzwungene Beladung",
                        ShortName: "Manuell",
                        Info: "In diesem Modus wird die Beladung des Autos erzwungen, d.h. es wird immer garantiert, dass das Auto geladen wird, auch wenn die Ladesäule auf Netzstrom zugreifen muss.",
                        MaxCharging: "Maximale Ladeleistung",
                        MaxChargingDetails: "Falls das Auto den eingegebenen Maximalwert nicht laden kann, wird die Leistung automatisch begrenzt."
                    }
                }
            }
        },
        History: {
            SelectedPeriod: "Gewählter Zeitraum: ",
            OtherPeriod: "Anderer Zeitraum",
            Period: "Zeitraum",
            SelectedDay: "{{value}}",
            Today: "Heute",
            Yesterday: "Gestern",
            LastWeek: "Letzte Woche",
            LastMonth: "Letzter Monat",
            LastYear: "Letztes Jahr",
            Go: "Los!",
            Export: "Download als EXCEL-Datei",
            Day: "Tag",
            Week: "Woche",
            Month: "Monat",
            Year: "Jahr",
            noData: "keine Daten verfügbar",
            activeDuration: "Einschaltdauer",
            BeginDate: "Startdatum wählen",
            EndDate: "Enddatum wählen",
            Sun: "So",
            Mon: "Mo",
            Tue: "Di",
            Wed: "Mi",
            Thu: "Do",
            Fri: "Fr",
            Sat: "Sa",
            Jan: "Jan",
            Feb: "Feb",
            Mar: "Mär",
            Apr: "Apr",
            May: "Mai",
            Jun: "Jun",
            Jul: "Jul",
            Aug: "Aug",
            Sep: "Sep",
            Oct: "Okt",
            Nov: "Nov",
            Dec: "Dez"
        },
        Config: {
            Index: {
                Bridge: "Verbindungen und Geräte",
                Scheduler: "Anwendungsplaner",
                Controller: "Anwendungen",
                Simulator: "Simulator",
                ExecuteSimulator: "Simulationen ausführen",
                Log: "Log",
                LiveLog: "Live Systemprotokoll",
                AddComponents: "Komponenten installieren",
                AdjustComponents: "Komponenten konfigurieren",
                ManualControl: "Manuelle Steuerung",
                DataStorage: "Datenspeicher",
                SystemExecute: "System-Befehl ausführen"
            },
            More: {
                ManualCommand: "Manueller Befehl",
                Send: "Senden",
                RefuInverter: "REFU Wechselrichter",
                RefuStartStop: "Wechselrichter starten/stoppen",
                RefuStart: "Starten",
                RefuStop: "Stoppen",
                ManualpqPowerSpecification: "Leistungsvorgabe",
                ManualpqSubmit: "Übernehmen",
                ManualpqReset: "Zurücksetzen"
            },
            Scheduler: {
                NewScheduler: "Neuer Scheduler...",
                Class: "Klasse:",
                NotImplemented: "Formular nicht implementiert: ",
                Contact: "Das sollte nicht passieren. Bitte kontaktieren Sie <a href=\"mailto:{{value}}\">{{value}}</a>.",
                Always: "Immer"
            },
            Log: {
                AutomaticUpdating: "Automatische Aktualisierung",
                Timestamp: "Zeitpunkt",
                Level: "Level",
                Source: "Quelle",
                Message: "Nachricht"
            },
            Controller: {
                InternallyID: "Interne ID:",
                App: "Anwendung:",
                Priority: "Priorität:"
            },
            Bridge: {
                NewDevice: "Neues Gerät...",
                NewConnection: "Neue Verbindung..."
            }
        }
    },
    About: {
        UI: "Benutzeroberfläche für OpenEMS",
        Developed: "Diese Benutzeroberfläche wird als Open-Source-Software entwickelt.",
        OpenEMS: "Mehr zu OpenEMS",
        CurrentDevelopments: "Aktuelle Entwicklungen",
        Build: "Dieser Build",
        Contact: "Für Rückfragen und Anregungen zum System, wenden Sie sich bitte an unser Team unter <a href=\"mailto:{{value}}\">{{value}}</a>.",
        Language: "Sprache wählen:"
    },
    Notifications: {
        Failed: "Verbindungsaufbau fehlgeschlagen.",
        LoggedInAs: "Angemeldet als Benutzer \"{{value}}\".", // value = username
        LoggedIn: "Angemeldet.",
        AuthenticationFailed: "Keine Verbindung: Authentifizierung fehlgeschlagen.",
        Closed: "Verbindung beendet."
    }
}
