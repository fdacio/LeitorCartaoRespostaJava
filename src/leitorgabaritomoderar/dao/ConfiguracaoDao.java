/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import javax.persistence.EntityManager;
import leitorgabaritomoderar.entity.Configuracao;

/**
 *
 * @author dacio
 */
public class ConfiguracaoDao extends GenericDao<Long, Configuracao> {
    public ConfiguracaoDao(EntityManager entityManager) {
        super(entityManager);
    }
}
