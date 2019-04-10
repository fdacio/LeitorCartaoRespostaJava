/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.forms;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import leitorgabaritomoderar.dao.FuncaoDao;
import leitorgabaritomoderar.dao.FuncaoService;
import leitorgabaritomoderar.dao.SimpleEntityManager;
import leitorgabaritomoderar.dialog.MessageDialog;
import leitorgabaritomoderar.entity.Funcao;

/**
 *
 * @author dacio
 */
public class FuncoesConsultaForm extends ListForm {

    private final FuncaoService funcaoService;

    public FuncoesConsultaForm(Frame parent, boolean modal) {
        super(parent, modal);
        funcaoService = new FuncaoService(new SimpleEntityManager());
    }

    @Override
    public JDialog setTitulo(String titulo) {
        this.setTitle(titulo);
        return this;
    }

    @Override
    public void onClickNovo() {
        new FuncoesEditForm(this.getParentFrame()).setVisible(true);
    }

    @Override
    public void onClickAlterar() {
        Long id = getSelectedId();
        new FuncoesEditForm(this.getParentFrame(), id).setVisible(true);
    }

    @Override
    public void onClickExcluir() {
        Long id = getSelectedId();
        int dialogButton = JOptionPane.YES_NO_OPTION;
        JOptionPane.showConfirmDialog(null, "Excluir Registro?", "Função", dialogButton);
        if (dialogButton == JOptionPane.YES_OPTION) {
            FuncaoDao funcaoDao = new FuncaoDao((new SimpleEntityManager()).getEntityManager());
            try {
                funcaoService.delete(funcaoDao.getById(id));
            } catch (Exception ex) {
                new MessageDialog(this.getParentFrame(), ex.getMessage()).setVisible(true);
            }
        }
    }

    @Override
    public void onClickConsultar() {
    }

    @Override
    public void setDataTable() {

        Object[][] dados = new Object[funcaoService.findAll().size()][2];
        int i = 0;
        for (Funcao funcao : funcaoService.findAll()) {
            dados[i][0] = funcao.getId();
            dados[i][1] = funcao.getNome();
            i++;
        }

        this.setData(dados);
        this.setColmnNames(new String[]{"ID", "Nome da Função"});
        this.setColumnWidths(new double[]{10, 90});
    }

}
