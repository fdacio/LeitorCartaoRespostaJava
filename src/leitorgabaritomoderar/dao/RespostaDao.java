/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import javax.persistence.EntityManager;
import leitorgabaritomoderar.entity.Resposta;

/**
 *
 * @author dacio
 */
public class RespostaDao extends GenericDao<Long, Resposta> {
    
    public RespostaDao(EntityManager entityManager) {
        super(entityManager);
    }
}
