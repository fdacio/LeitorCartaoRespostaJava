/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.dao;

import java.util.ArrayList;
import java.util.List;
import leitorgabaritomoderar.entity.Funcao;

/**
 *
 * @author dacio
 */
public class FuncaoService {

    private final FuncaoDao dao;

    private final SimpleEntityManager simpleEntityManager;

    public FuncaoService(SimpleEntityManager simpleEntityManager) {
        this.simpleEntityManager = simpleEntityManager;
        dao = new FuncaoDao(simpleEntityManager.getEntityManager());
    }

    public void save(Funcao funcao) throws Exception {
        try {
            this.simpleEntityManager.beginTransaction();
            funcao.validate();
            if (funcao.getId() == null) {
                dao.save(funcao);
            } else {
                dao.update(funcao);
            }
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
            throw new Exception(e.getMessage());
        }
    }
    
    public void delete(Funcao funcao) throws Exception {
        try {
            this.simpleEntityManager.beginTransaction();
            dao.delete(funcao);
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
            throw new Exception(e.getMessage());
        }
    }

    public List<Funcao> findAll() {
       List<Funcao> lista = new ArrayList<>();
        try {
            this.simpleEntityManager.beginTransaction();
            lista = dao.findAll();
            this.simpleEntityManager.commit();
        } catch (Exception e) {
            this.simpleEntityManager.rollBack();
        }
        return lista;
    }

}
