package org.example.sklep.entity;

public enum Code {
    SUCCESS("Operation end success"),
    PERMIT("Przyznano dostep"),
    A1("Podany uzytkownik o danej nazwie nie istnieje lub nie aktywował konta"),
    A2("Podane dane są nieprawidłowe"),
    A3("Wskazany token jest pusty lub nie ważny"),
    A4("Uzytkownik o tej nazwie juz istnieje"),
    A5("Uzytkownik o tym emailu juz istenieje");



    public final String label;
    private Code(String label){
        this.label =label;
    }
}
