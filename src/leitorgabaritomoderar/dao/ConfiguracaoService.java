/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import leitorgabaritomoderar.entity.Configuracao;

/**
 *
 * @author dacio
 */
public class ConfiguracaoService {
    
    private final ConfiguracaoDao dao;

    private final SimpleEntityManager simpleEntityManager;

    public ConfiguracaoService(SimpleEntityManager simpleEntityManager) {
        this.simpleEntityManager = simpleEntityManager;
        dao = new ConfiguracaoDao(simpleEntityManager.getEntityManager());
    }

    public void save(Configuracao configuracao) throws Exception {
        try {
            this.simpleEntityManager.beginTransaction();
            if (configuracao.getId() == null) {
                dao.save(configuracao);
            } else {
                dao.update(configuracao);
            }
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
            throw new Exception(e.getMessage());
        }
    }
    
    public void delete(Configuracao configuracao) throws Exception {
        try {
            this.simpleEntityManager.beginTransaction();
            dao.delete(configuracao);
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
            throw new Exception(e.getMessage());
        }
    }

    public Configuracao find() {
        try {
            EntityManager em = this.simpleEntityManager.getEntityManager();
            TypedQuery<Configuracao> query;
            query = em.createQuery("Select c From Configuracao c", Configuracao.class);
            Configuracao configuracao;
            configuracao = query.getSingleResult();
            return configuracao;
        } catch (Exception e) {
            return new Configuracao();
        }
    }

}
