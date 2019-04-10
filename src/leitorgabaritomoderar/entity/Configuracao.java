/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author dacio
 */
@Entity
public class Configuracao implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String tipoArquivo;
    @Column
    private String tessLanguage;
    @Column
    private String tessDataPath;

    public Configuracao() {
        this.tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";
        this.tessLanguage = "eng";
        this.tipoArquivo = "jpg";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    public String getTipoArquivo(){
        return this.tipoArquivo;
    }
    
    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }
    
    public String getTessLanguage() {
        return this.tessLanguage;
    }
    
    public void setTessLanguage(String tessLanguage){
        this.tessLanguage = tessLanguage;
    }

    public String getTessDataPath() {
        return this.tessDataPath;
    } 
    
    public void setTessDataPath(String tessDataPath) {
        this.tessDataPath = tessDataPath;
    }
    
}
