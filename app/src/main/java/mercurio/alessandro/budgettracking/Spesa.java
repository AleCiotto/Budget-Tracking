package mercurio.alessandro.budgettracking;

import java.util.Calendar;

/**
 * Created by Alessandro on 05/08/2014.
 */
public class Spesa {
    String nome;
    Calendar data;
    String prezzo;
    String luogo;
    String categoria;
    String descrizione;
    int img_url;
    String img_url_custom;
    boolean alert;
    long id;

    public Spesa(String nome, Calendar data, String prezzo, String categoria, long id) {
        this.nome = nome;
        this.data = data;
        this.prezzo = prezzo;
        this.categoria = categoria;
        this.id = id;
    }

    public Spesa(String nome, Calendar data, String prezzo, String luogo, String categoria, String descrizione, int id) {
        this.nome = nome;
        this.data = data;
        this.prezzo = prezzo;
        this.luogo = luogo;
        this.categoria = categoria;
        this.descrizione = descrizione;
        this.id = id;
    }

    public Spesa(String nome, Calendar data, String prezzo, String luogo, String categoria, String descrizione, int img_url, String img_url_custom, boolean alert, long id) {
        this.nome = nome;
        this.data = data;
        this.prezzo = prezzo;
        this.luogo = luogo;
        this.categoria = categoria;

        this.descrizione = descrizione;
        this.img_url_custom = img_url_custom;
        this.img_url = img_url;
        this.alert = alert;
        this.id = id;
    }

    public Spesa(String nome, Calendar data, String prezzo, String luogo, String categoria, String descrizione, int img_url, String img_url_custom, boolean alert) {
        this.nome = nome;
        this.data = data;
        this.prezzo = prezzo;
        this.luogo = luogo;
        this.categoria = categoria;

        this.descrizione = descrizione;
        this.img_url_custom = img_url_custom;

        this.img_url = img_url;
        this.alert = alert;
    }

    public void Spesa() {
    }

    public int getImg_url() {
        return img_url;
    }

    public void setImg_url(int img_url) {
        this.img_url = img_url;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public String getImg_url_custom() {
        return img_url_custom;
    }

    public void setImg_url_custom(String img_url_custom) {
        this.img_url_custom = img_url_custom;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Calendar getData() {
        return data;
    }

    public void setData(Calendar data) {
        this.data = data;
    }

    public String getDataString() {
        String mese = "error";
        switch (data.get(Calendar.MONTH)) {
            case 0: {
                mese = "Gennaio";
                break;
            }
            case 1: {
                mese = "Febbraio";
                break;
            }
            case 2: {
                mese = "Marzo";
                break;
            }
            case 3: {
                mese = "Aprile";
                break;
            }
            case 4: {
                mese = "Maggio";
                break;
            }
            case 5: {
                mese = "Giugno";
                break;
            }
            case 6: {
                mese = "Luglio";
                break;
            }
            case 7: {
                mese = "Agosto";
                break;
            }
            case 8: {
                mese = "Settembre";
                break;
            }
            case 9: {
                mese = "Ottobre";
                break;
            }
            case 10: {
                mese = "Novembre";
                break;
            }
            case 11: {
                mese = "Dicembre";
                break;
            }
            default:
                mese = "undefined";
        }
        return (data.get(Calendar.DAY_OF_MONTH) + " " + mese + " " + data.get(Calendar.YEAR));
    }

    public String getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(String prezzo) {
        this.prezzo = prezzo;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getImg() {
        return img_url;
    }

    public void setImg(int img) {
        this.img_url = img_url;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}