/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leitorgabaritomoderar.forms;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import leitorgabaritomoderar.dao.ConfiguracaoService;
import leitorgabaritomoderar.dao.FuncaoService;
import leitorgabaritomoderar.dao.RespostaDao;
import leitorgabaritomoderar.dao.RespostaService;
import leitorgabaritomoderar.dao.SimpleEntityManager;
import leitorgabaritomoderar.dialog.MessageDialog;
import leitorgabaritomoderar.dialog.ProgressDialog;
import leitorgabaritomoderar.entity.Configuracao;
import leitorgabaritomoderar.entity.Funcao;
import leitorgabaritomoderar.entity.Resposta;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 *
 * @author dacio
 */
public class MainFrame extends javax.swing.JFrame {
    
    private final Configuracao configuracao;
    private FileWriter fwOCR;
    private String fileWriteOCR = "";
    private FileWriter fwLimpo;
    private String fileWriteLimpo = "";

    private String limpaOCR(String textoOCR) {
        String letras = "ABCDE-";
        String numeros = "0123456789";
        String linha;
        String novoTexto = "";
        int nLinha = 1;
        try (Scanner scanner = new Scanner(textoOCR)) {
            while ((linha = scanner.nextLine()) != null) {
                String novaLinha = "";
                for (int i = 0; i < linha.length(); i++) {
                    char c = linha.charAt(i);
                    if (nLinha == 1) {
                        if (numeros.indexOf(c) > -1) {
                            novaLinha += c;
                        }
                    } else if ((numeros.indexOf(c) > -1) || (letras.indexOf(c) > -1)) {
                        novaLinha += c;
                    }
                }
                if (!novaLinha.equals("")) {
                    novoTexto += novaLinha + "\n";
                }
                nLinha++;
            }
        } catch (Exception e) {
        }

        return novoTexto;
    }

    private List<String> geraListaQuestoesAlternativas(String textoProcessado) {

        String letras = "ABCDE";
        String numeros = "0123456789";
        String linha;
        String token = "";
        List<String> listaQuestoesAlternativas = new ArrayList<>();

        try (Scanner scanner = new Scanner(textoProcessado)) {
            while ((linha = scanner.nextLine()) != null) {
                linha += '-';
                int i = 0;
                int lengthLinha = linha.length();
                while (i < lengthLinha) {
                    char c = linha.charAt(i);
                    if ((c == '-') && (!token.equals("-"))) {
                        listaQuestoesAlternativas.add(token);
                        token = "";
                    }
                    if ((numeros.indexOf(c) > -1) || (letras.indexOf(c) > -1)) {
                        token += c;
                    }
                    i++;
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(listaQuestoesAlternativas);
        return (listaQuestoesAlternativas);
    }

    private char getResposta(String token) {

        char[] alternativas = {'A', 'B', 'C', 'D', 'E'};

        if (token.length() != 6) {
            return '0';
        }

        for (char candidate : alternativas) {
            if (token.indexOf(candidate) == -1) {
                return candidate;
            }
        }

        return '0';
    }

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {

        initComponents();
        
        configuracao = (new ConfiguracaoService(new SimpleEntityManager())).find();

        jTabbedPane1.addChangeListener((ChangeEvent e) -> {
            if (jTabbedPane1.getSelectedIndex() == 2) {
                updateTableRespostas();
            }
        }
        );
    }

    private void updateTableRespostas() {
        ProgressDialog progressDialog = new ProgressDialog(MainFrame.this, "Aguarde...");
        Thread thread;
        thread = new Thread() {

            @Override
            public void run() {
                progressDialog.setVisible(true);
                DefaultTableModel modelData = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int rowIndex, int mColIndex) {
                        return false;
                    }
                };

                modelData.addColumn("Id");
                modelData.addColumn("Inscrição");
                modelData.addColumn("Função");
                modelData.addColumn("Respostas");

                SimpleEntityManager sem = new SimpleEntityManager();

                RespostaService respostaService = new RespostaService(sem);
                List<Resposta> lista = respostaService.findNotIsFile();
                Object[][] dados = new Object[lista.size()][4];
                int i = 0;
                for (Resposta resposta : lista) {
                    dados[i][0] = resposta.getId();
                    dados[i][1] = resposta.getInscricao();
                    dados[i][2] = resposta.getFuncao().getNome();
                    dados[i][3] = resposta.getRespostas();
                    i++;
                }

                for (Object object[] : dados) {
                    modelData.addRow(object);
                }
                jTableRespostas.setModel(modelData);

                jTableRespostas.getColumnModel().getColumn(0).setPreferredWidth(jTableRespostas.getWidth() * 10 / 100);
                jTableRespostas.getColumnModel().getColumn(1).setPreferredWidth(jTableRespostas.getWidth() * 20 / 100);
                jTableRespostas.getColumnModel().getColumn(2).setPreferredWidth(jTableRespostas.getWidth() * 20 / 100);
                jTableRespostas.getColumnModel().getColumn(3).setPreferredWidth(jTableRespostas.getWidth() * 50 / 100);

                DefaultComboBoxModel modelDataFuncao = new DefaultComboBoxModel();
                FuncaoService funcaoService = new FuncaoService(sem);

                funcaoService.findAll()
                        .forEach((funcao) -> {
                            modelDataFuncao.addElement(funcao.getId() + "-" + funcao.getNome());
                        }
                        );
                jComboBoxFuncao.setModel(modelDataFuncao);

                progressDialog.setVisible(
                        false);
                MainFrame.this.setEnabled(
                        true);
            }
        };
        thread.start();
    }

    private void addResposta() {

        if (jTextFieldResposta.getText().equals("")) {
            return;
        }

        Resposta resposta = new Resposta();
        resposta.setInscricao(jTextFieldResposta.getText().substring(0, 7));
        resposta.setRespostas(jTextFieldResposta.getText().substring(8));
        FuncaoService funcaoService = new FuncaoService(new SimpleEntityManager());
        Funcao funcao = funcaoService.findAll().get(jComboBoxFuncao.getSelectedIndex());
        resposta.setFuncao(funcao);

        RespostaService respostaService = new RespostaService(new SimpleEntityManager());
        try {
            respostaService.save(resposta);
        } catch (Exception ex) {
            new MessageDialog(this, ex.getMessage()).setVisible(true);
        }

    }

    private void removeResposta() {

        int column = 0;
        int row = jTableRespostas.getSelectedRow();
        String value = jTableRespostas.getModel().getValueAt(row, column).toString();
        Long id = Long.parseLong(value);
        RespostaService respostaService = new RespostaService(new SimpleEntityManager());
        RespostaDao respostaDao = new RespostaDao((new SimpleEntityManager()).getEntityManager());
        Resposta resposta = respostaDao.getById(id);
        try {
            respostaService.delete(resposta);
        } catch (Exception ex) {
            new MessageDialog(this, ex.getMessage()).setVisible(true);
        }
    }

    private void saveFileResposta() {

        FileFilter filter = new FileNameExtensionFilter("Salvar Arquivo Resposta", "rpt");
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar Arquivo Resposta");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            RespostaService respostaService = new RespostaService(new SimpleEntityManager());
            List<Resposta> lista = respostaService.findNotIsFile();
            
            String extensao = ((chooser.getSelectedFile().toString().contains(".rpt")) ? "" : ".rpt");
            try {
                try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + extensao)) {
                    for (Resposta resposta : lista) {
                        fw.write(resposta.getInscricao() + resposta.getRespostas() + "\n");
                    }
                    fw.close();
                }

            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            
            lista.stream().map((resposta) -> {
                resposta.setArquivo(true);
                return resposta;
            }).forEachOrdered((resposta) -> {
                try {
                    respostaService.save(resposta);
                } catch (Exception ex) {
                    new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
                }
            });
            
            updateTableRespostas();
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialogProgress = new javax.swing.JDialog();
        jFrame1 = new javax.swing.JFrame();
        jSeparator2 = new javax.swing.JSeparator();
        jPanelToolBar = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonFazOCR = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton4 = new javax.swing.JButton();
        jButtonConfiguracoes = new javax.swing.JButton();
        jButtonSair = new javax.swing.JButton();
        jPanelSelecaoDiretorio = new javax.swing.JPanel();
        edtArquivoCartaoResposta = new javax.swing.JTextField();
        jButtonOpenCartaoResposta = new javax.swing.JButton();
        jPanelImageCartaoResposta = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        imgCartaoResposta = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaOCR = new javax.swing.JTextArea();
        jToolBar2 = new javax.swing.JToolBar();
        jButtonLimpaOCR = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jButtonSaveOCR = new javax.swing.JButton();
        jButtonOpenOCR = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jLabelNomeArquivoOCR = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaLimpo = new javax.swing.JTextArea();
        jToolBar3 = new javax.swing.JToolBar();
        jButtonGeraRespostas = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jButtonSaveOCRLimpo = new javax.swing.JButton();
        jButtonOpenOCRLimpo = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jLabelNomeArquivoLimpo = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableRespostas = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jTextFieldResposta = new javax.swing.JTextField();
        jComboBoxFuncao = new javax.swing.JComboBox<>();
        jButtonAddResposta = new javax.swing.JButton();
        jToolBar4 = new javax.swing.JToolBar();
        jButtonSaveResposta = new javax.swing.JButton();
        jButtonRemoveResposta = new javax.swing.JButton();

        jDialogProgress.setTitle("Aguarde...");
        jDialogProgress.setModal(true);
        jDialogProgress.setName("dialogProgress"); // NOI18N

        javax.swing.GroupLayout jDialogProgressLayout = new javax.swing.GroupLayout(jDialogProgress.getContentPane());
        jDialogProgress.getContentPane().setLayout(jDialogProgressLayout);
        jDialogProgressLayout.setHorizontalGroup(
            jDialogProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialogProgressLayout.setVerticalGroup(
            jDialogProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Leitor de Gabarito - Moderar");

        jPanelToolBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jToolBar1.setFloatable(false);

        jButtonFazOCR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/credit-card-scan.png"))); // NOI18N
        jButtonFazOCR.setFocusable(false);
        jButtonFazOCR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonFazOCR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonFazOCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFazOCRActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonFazOCR);
        jToolBar1.add(jSeparator1);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/briefcase-account.png"))); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButtonConfiguracoes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/settings.png"))); // NOI18N
        jButtonConfiguracoes.setFocusable(false);
        jButtonConfiguracoes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonConfiguracoes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonConfiguracoes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfiguracoesActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonConfiguracoes);

        jButtonSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/exit-run.png"))); // NOI18N
        jButtonSair.setFocusable(false);
        jButtonSair.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSair.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSairActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSair);

        javax.swing.GroupLayout jPanelToolBarLayout = new javax.swing.GroupLayout(jPanelToolBar);
        jPanelToolBar.setLayout(jPanelToolBarLayout);
        jPanelToolBarLayout.setHorizontalGroup(
            jPanelToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelToolBarLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanelToolBarLayout.setVerticalGroup(
            jPanelToolBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelToolBarLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanelSelecaoDiretorio.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Informe o Cartão Resposta", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jButtonOpenCartaoResposta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/folder-open-outline-24.png"))); // NOI18N
        jButtonOpenCartaoResposta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenCartaoRespostaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelSelecaoDiretorioLayout = new javax.swing.GroupLayout(jPanelSelecaoDiretorio);
        jPanelSelecaoDiretorio.setLayout(jPanelSelecaoDiretorioLayout);
        jPanelSelecaoDiretorioLayout.setHorizontalGroup(
            jPanelSelecaoDiretorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSelecaoDiretorioLayout.createSequentialGroup()
                .addComponent(edtArquivoCartaoResposta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenCartaoResposta))
        );
        jPanelSelecaoDiretorioLayout.setVerticalGroup(
            jPanelSelecaoDiretorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSelecaoDiretorioLayout.createSequentialGroup()
                .addGroup(jPanelSelecaoDiretorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonOpenCartaoResposta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(edtArquivoCartaoResposta))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelImageCartaoResposta.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Cartão Resposta ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 14))); // NOI18N

        jScrollPane2.setViewportView(imgCartaoResposta);

        javax.swing.GroupLayout jPanelImageCartaoRespostaLayout = new javax.swing.GroupLayout(jPanelImageCartaoResposta);
        jPanelImageCartaoResposta.setLayout(jPanelImageCartaoRespostaLayout);
        jPanelImageCartaoRespostaLayout.setHorizontalGroup(
            jPanelImageCartaoRespostaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelImageCartaoRespostaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelImageCartaoRespostaLayout.setVerticalGroup(
            jPanelImageCartaoRespostaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelImageCartaoRespostaLayout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jTextAreaOCR.setColumns(20);
        jTextAreaOCR.setRows(5);
        jScrollPane3.setViewportView(jTextAreaOCR);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jButtonLimpaOCR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/file-replace.png"))); // NOI18N
        jButtonLimpaOCR.setFocusable(false);
        jButtonLimpaOCR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLimpaOCR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonLimpaOCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLimpaOCRActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonLimpaOCR);
        jToolBar2.add(jSeparator4);

        jButtonSaveOCR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/content-save-24.png"))); // NOI18N
        jButtonSaveOCR.setFocusable(false);
        jButtonSaveOCR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSaveOCR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSaveOCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveOCRActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonSaveOCR);

        jButtonOpenOCR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/folder-open-outline-24.png"))); // NOI18N
        jButtonOpenOCR.setFocusable(false);
        jButtonOpenOCR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpenOCR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpenOCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenOCRActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonOpenOCR);
        jToolBar2.add(jSeparator6);

        jLabelNomeArquivoOCR.setText("Arquivo OCR");
        jToolBar2.add(jLabelNomeArquivoOCR);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Texto OCR Puro", jPanel1);

        jTextAreaLimpo.setColumns(20);
        jTextAreaLimpo.setRows(5);
        jScrollPane4.setViewportView(jTextAreaLimpo);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        jButtonGeraRespostas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/file-replace.png"))); // NOI18N
        jButtonGeraRespostas.setFocusable(false);
        jButtonGeraRespostas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGeraRespostas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGeraRespostas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGeraRespostasActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonGeraRespostas);
        jToolBar3.add(jSeparator7);

        jButtonSaveOCRLimpo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/content-save-24.png"))); // NOI18N
        jButtonSaveOCRLimpo.setFocusable(false);
        jButtonSaveOCRLimpo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSaveOCRLimpo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSaveOCRLimpo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveOCRLimpoActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonSaveOCRLimpo);

        jButtonOpenOCRLimpo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/folder-open-outline-24.png"))); // NOI18N
        jButtonOpenOCRLimpo.setFocusable(false);
        jButtonOpenOCRLimpo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpenOCRLimpo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpenOCRLimpo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenOCRLimpoActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonOpenOCRLimpo);
        jToolBar3.add(jSeparator5);

        jLabelNomeArquivoLimpo.setText("Arquivo Limpo");
        jToolBar3.add(jLabelNomeArquivoLimpo);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Texto Limpo", jPanel3);

        jTableRespostas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableRespostas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTableRespostas);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButtonAddResposta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/plus-box.png"))); // NOI18N
        jButtonAddResposta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddRespostaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextFieldResposta)
            .addComponent(jComboBoxFuncao, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonAddResposta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                .addComponent(jTextFieldResposta, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxFuncao, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonAddResposta)
                .addContainerGap())
        );

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        jButtonSaveResposta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/content-save-24.png"))); // NOI18N
        jButtonSaveResposta.setFocusable(false);
        jButtonSaveResposta.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSaveResposta.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSaveResposta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveRespostaActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonSaveResposta);

        jButtonRemoveResposta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icones/trash-can-outline.png"))); // NOI18N
        jButtonRemoveResposta.setFocusable(false);
        jButtonRemoveResposta.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRemoveResposta.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRemoveResposta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveRespostaActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonRemoveResposta);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(160, 160, 160))
        );

        jTabbedPane1.addTab("Respostas", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanelImageCartaoResposta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane1))
                    .addComponent(jPanelSelecaoDiretorio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelSelecaoDiretorio, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelImageCartaoResposta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanelSelecaoDiretorio.getAccessibleContext().setAccessibleName("Informe");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOpenCartaoRespostaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenCartaoRespostaActionPerformed

        FileFilter filter = new FileNameExtensionFilter("Cartões Resposta", configuracao.getTipoArquivo());
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Informe o Cartão Resposta do Candidato");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            edtArquivoCartaoResposta.setText(chooser.getSelectedFile().toString());
            try {
                BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                imgCartaoResposta.setIcon(new ImageIcon(img));
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
        }
    }//GEN-LAST:event_jButtonOpenCartaoRespostaActionPerformed

    private void jButtonFazOCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFazOCRActionPerformed

        File arquivoCartaoResposta = new File(edtArquivoCartaoResposta.getText());
        if (!arquivoCartaoResposta.exists()) {
            return;
        }

        jTabbedPane1.setSelectedIndex(0);

        Tesseract tess = new Tesseract();
        tess.setLanguage(configuracao.getTessLanguage());
        tess.setDatapath(configuracao.getTessDataPath());
        tess.setTessVariable("tessedit_char_whitelist", "0123456789ABCDE-");
        tess.setTessVariable("tessedit_char_blacklist", "FGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        JDialog progressDialog = new ProgressDialog(this, "Processando...");

        Thread thread;
        thread = new Thread() {
            @Override
            public void run() {

                progressDialog.setVisible(true);

                jTextAreaOCR.setText("");
                jTextAreaLimpo.setText("");
                fwOCR = null;
                fwLimpo = null;
                jLabelNomeArquivoOCR.setText("Arquivo OCR");
                jLabelNomeArquivoLimpo.setText("Arquivo Limpo");

                try {
                    String ocr = tess.doOCR(arquivoCartaoResposta);
                    jTextAreaOCR.append(ocr);
                } catch (TesseractException ex) {
                    new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
                }

                progressDialog.setVisible(false);
                MainFrame.this.setEnabled(true);

            }
        };
        thread.start();
    }//GEN-LAST:event_jButtonFazOCRActionPerformed

    private void jButtonConfiguracoesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfiguracoesActionPerformed

        JDialog configuracoesForm = new ConfiguracaoForm(this);
        configuracoesForm.setVisible(true);
    }//GEN-LAST:event_jButtonConfiguracoesActionPerformed

    private void jButtonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSairActionPerformed

        System.exit(0);
    }//GEN-LAST:event_jButtonSairActionPerformed

    private void jButtonOpenOCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenOCRActionPerformed

        FileFilter filter = new FileNameExtensionFilter("Abrir Arquivo OCR", "ocr");
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir Arquivo OCR");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (FileReader arq = new FileReader(chooser.getSelectedFile().toString())) {
                BufferedReader lerArq = new BufferedReader(arq);
                String linha = lerArq.readLine();
                jTextAreaOCR.setText("");
                StringBuilder sb = new StringBuilder();
                while (linha != null) {
                    sb.append(linha).append("\n");
                    linha = lerArq.readLine();
                }
                jTextAreaOCR.setText(sb.toString());
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            fileWriteOCR = chooser.getSelectedFile().toString();
            jLabelNomeArquivoOCR.setText(fileWriteOCR);
        }
    }//GEN-LAST:event_jButtonOpenOCRActionPerformed

    private void jButtonSaveOCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveOCRActionPerformed

        if ((fwOCR != null) && (!fileWriteOCR.equals(""))) {
            try {
                fwOCR = new FileWriter(fileWriteOCR);
                fwOCR.write(jTextAreaOCR.getText());
                fwOCR.close();
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            return;
        }

        FileFilter filter = new FileNameExtensionFilter("Salvar Arquivo OCR", "ocr");
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar Arquivo OCR");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                fwOCR = new FileWriter(chooser.getSelectedFile() + ((chooser.getSelectedFile().toString().contains(".ocr")) ? "" : ".ocr"));
                fwOCR.write(jTextAreaOCR.getText());
                fwOCR.close();
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
        }
        fileWriteOCR = chooser.getSelectedFile().toString();
        jLabelNomeArquivoOCR.setText(fileWriteOCR);
    }//GEN-LAST:event_jButtonSaveOCRActionPerformed

    private void jButtonOpenOCRLimpoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenOCRLimpoActionPerformed

        FileFilter filter = new FileNameExtensionFilter("Abrir Arquivo OCR Limpo", "lmp");
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir Arquivo OCR Limpo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (FileReader arq = new FileReader(chooser.getSelectedFile().toString())) {
                BufferedReader lerArq = new BufferedReader(arq);
                String linha = lerArq.readLine();
                jTextAreaLimpo.setText("");
                StringBuilder sb = new StringBuilder();
                while (linha != null) {
                    sb.append(linha).append("\n");
                    linha = lerArq.readLine();
                }
                jTextAreaLimpo.setText(sb.toString());
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            fileWriteLimpo = chooser.getSelectedFile().toString();
            jLabelNomeArquivoLimpo.setText(fileWriteLimpo);
        }
    }//GEN-LAST:event_jButtonOpenOCRLimpoActionPerformed

    private void jButtonSaveOCRLimpoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveOCRLimpoActionPerformed

        if ((fwLimpo != null) && (!fwLimpo.equals(""))) {
            try {
                fwLimpo = new FileWriter(fileWriteLimpo);
                fwLimpo.write(jTextAreaLimpo.getText());
                fwLimpo.flush();
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            return;
        }

        FileFilter filter = new FileNameExtensionFilter("Salvar Arquivo OCR Limpo", "lmp");
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar Arquivo OCR Limpo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                fwLimpo = new FileWriter(chooser.getSelectedFile() + ((chooser.getSelectedFile().toString().contains(".lmp")) ? "" : ".lmp"));
                fwLimpo.write(jTextAreaLimpo.getText());
                fwLimpo.flush();
            } catch (IOException ex) {
                new MessageDialog(MainFrame.this, ex.getMessage()).setVisible(true);
            }
            fileWriteLimpo = chooser.getSelectedFile().toString();
            jLabelNomeArquivoLimpo.setText(fileWriteLimpo);
        }
    }//GEN-LAST:event_jButtonSaveOCRLimpoActionPerformed

    private void jButtonLimpaOCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLimpaOCRActionPerformed
        // TODO add your handling code here:
        String processa = limpaOCR(jTextAreaOCR.getText());
        jTextAreaLimpo.setText(processa);
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButtonLimpaOCRActionPerformed

    private void jButtonGeraRespostasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGeraRespostasActionPerformed
        // TODO add your handling code here:
        List<String> listaTokens = geraListaQuestoesAlternativas(jTextAreaLimpo.getText());
        int i = 1;
        String linha = "";
        for (String token : listaTokens) {
            if (token.trim().length() > 0) {
                if (i == 1) {
                    linha = token;
                } else {
                    linha += getResposta(token);
                }
                i++;
            }
        }
        jTextFieldResposta.setText(linha);
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButtonGeraRespostasActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        new FuncoesConsultaForm(MainFrame.this, true).setTitulo("Funções").setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButtonAddRespostaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddRespostaActionPerformed
        addResposta();
        updateTableRespostas();
    }//GEN-LAST:event_jButtonAddRespostaActionPerformed

    private void jButtonRemoveRespostaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveRespostaActionPerformed
        removeResposta();
        updateTableRespostas();
    }//GEN-LAST:event_jButtonRemoveRespostaActionPerformed

    private void jButtonSaveRespostaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveRespostaActionPerformed

        saveFileResposta();
    }//GEN-LAST:event_jButtonSaveRespostaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField edtArquivoCartaoResposta;
    private javax.swing.JLabel imgCartaoResposta;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButtonAddResposta;
    private javax.swing.JButton jButtonConfiguracoes;
    private javax.swing.JButton jButtonFazOCR;
    private javax.swing.JButton jButtonGeraRespostas;
    private javax.swing.JButton jButtonLimpaOCR;
    private javax.swing.JButton jButtonOpenCartaoResposta;
    private javax.swing.JButton jButtonOpenOCR;
    private javax.swing.JButton jButtonOpenOCRLimpo;
    private javax.swing.JButton jButtonRemoveResposta;
    private javax.swing.JButton jButtonSair;
    private javax.swing.JButton jButtonSaveOCR;
    private javax.swing.JButton jButtonSaveOCRLimpo;
    private javax.swing.JButton jButtonSaveResposta;
    private javax.swing.JComboBox<String> jComboBoxFuncao;
    private javax.swing.JDialog jDialogProgress;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabelNomeArquivoLimpo;
    private javax.swing.JLabel jLabelNomeArquivoOCR;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelImageCartaoResposta;
    private javax.swing.JPanel jPanelSelecaoDiretorio;
    private javax.swing.JPanel jPanelToolBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableRespostas;
    private javax.swing.JTextArea jTextAreaLimpo;
    private javax.swing.JTextArea jTextAreaOCR;
    private javax.swing.JTextField jTextFieldResposta;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    // End of variables declaration//GEN-END:variables
}
