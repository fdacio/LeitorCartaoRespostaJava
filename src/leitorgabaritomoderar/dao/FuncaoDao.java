/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import javax.persistence.EntityManager;
import leitorgabaritomoderar.entity.Funcao;

/**
 *
 * @author dacio
 */
public class FuncaoDao extends GenericDao<Long, Funcao> {
    public FuncaoDao(EntityManager entityManager) {
        super(entityManager);
    }
}
