/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;
import leitorgabaritomoderar.entity.Resposta;

/**
 *
 * @author dacio
 */
public class RespostaService {

    private final RespostaDao dao;

    private final SimpleEntityManager simpleEntityManager;

    public RespostaService(SimpleEntityManager simpleEntityManager) {
        this.simpleEntityManager = simpleEntityManager;
        dao = new RespostaDao(simpleEntityManager.getEntityManager());
    }

    public void save(Resposta resposta) throws Exception {
        try {
            
            this.simpleEntityManager.beginTransaction();
            
            if (resposta.getId() == null) {
                dao.save(resposta);
            } else {
                dao.update(resposta);
            }
            
            this.simpleEntityManager.commit();
            
        } catch (Exception e) {
            
            this.simpleEntityManager.rollBack();
            
            throw new Exception(e.getMessage());
        }
    }
    
    public void delete(Resposta resposta) throws Exception {
        try {
            this.simpleEntityManager.beginTransaction();
            dao.delete(resposta);
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
            throw new Exception(e.getMessage());
        }
    }

    public List<Resposta> findAll() {
       List<Resposta> lista = new ArrayList<>();
        try {
            this.simpleEntityManager.beginTransaction();
            lista = dao.findAll();
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
        }
        return lista;
    }
    
    public List<Resposta> findNotIsFile() {
        TypedQuery<Resposta>  query;
        query = this.simpleEntityManager.getEntityManager().createQuery("Select r FROM Resposta r Where r.arquivo = :arquivo", Resposta.class);
        return query.setParameter("arquivo", false).getResultList();
    }

    public List<Resposta> findIsFile() {
        TypedQuery<Resposta>  query;
        query = this.simpleEntityManager.getEntityManager().createQuery("Select r FROM Resposta r Where r.arquivo = :arquivo", Resposta.class);
        return query.setParameter("arquivo", true).getResultList();
    }
}
