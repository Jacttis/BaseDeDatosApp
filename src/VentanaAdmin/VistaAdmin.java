package VentanaAdmin;

import quick.dbtable.DBTable;
import Login.VistaLogin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class VistaAdmin extends JFrame {

    private JPanel contentPane;
    private JTextArea txtConsulta;
    private JButton btnBorrar;
    private JButton btnEjecutar;
    private JButton menuPrincipal;
    private DBTable tabla;
    private JList listaTablas;
    private DefaultListModel<String> dListaTabla;
    private JList<String> listaColumnas;
    private DefaultListModel<String> dListaColumnas;
    private Admin admin;


    public VistaAdmin(DBTable tabla){
        super("Panel Admin");
        admin=new Admin(tabla);
        setVisible(true);
        setResizable(false);
        setPreferredSize(new Dimension(1028, 600));
        this.setBounds(600, 300, 1028, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel del JFRAME de admin
        contentPane=new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        //Area para escribir los comando SQL
        txtConsulta=new JTextArea();
        txtConsulta.setBounds(0,3,600,70);
        contentPane.add(txtConsulta);

        //Boton Ejecutar
        btnEjecutar =new JButton("Ejecutar");
        btnEjecutar.setBounds(601,0,100,35);
        contentPane.add(btnEjecutar);
        btnEjecutar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEjecutarActionPerformed(e);
            }
        });

        //Boton Borrar
        btnBorrar =new JButton("Borrar");
        btnBorrar.setBounds(601,35,100,35);
        contentPane.add(btnBorrar);

        //Boton menuPrincipal
        menuPrincipal = new JButton("Menu Principal");
        menuPrincipal.setBounds(710, 0,180, 35);
        menuPrincipal.setEnabled(true);
        menuPrincipal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VistaLogin vistaL = new VistaLogin();
                try {
                    tabla.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                dispose();
            }
        });
        contentPane.add((menuPrincipal));



        //Tabla de la base de datos pasada por parametros
        this.tabla=tabla;
        tabla.setBounds(0,100,701,460);
        contentPane.add(tabla);

        //Lista con todas los atributos de la tabla elegida
        listaColumnas=new JList<String>();
        listaColumnas.setBounds(868,100,162,262);
        contentPane.add(listaColumnas);



        //Lista con todas las tablas de la base de datos
        dListaTabla=admin.crearLista("SHOW TABLES","Tables_in_parquimetros");
        listaTablas=new JList(dListaTabla);
        listaTablas.setBounds(705,100,162,262);
        listaTablas.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        contentPane.add(listaTablas);

        listaTablas.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    dListaColumnas=admin.crearLista("describe "+dListaTabla.getElementAt(e.getLastIndex()),"Field");
                    listaColumnas.setModel(dListaColumnas);
                    listaTablas.clearSelection();
                }
            }
        });






    }

    /**
     * Action performed del boton ejecutar que toma la consulta y la ejecuta
     * @param evt
     */
    private void btnEjecutarActionPerformed(ActionEvent evt) {
        this.refrescarTabla();
    }

    /**
     * Refresca la tabla con la consulta obtenida en el JTextArea(Dado por la catedra)
     */
    private void refrescarTabla()
    {
        try {
            String sql = txtConsulta.getText();
            Connection c = tabla.getConnection();
            Statement st = c.createStatement();
            st.execute(sql.trim());
            ResultSet rs = st.getResultSet();
            if (rs!=null && rs.next())
                tabla.refresh(rs);
            else
                if(rs!=null)
                    tabla.refresh();
                else
                    JOptionPane.showMessageDialog(null,"La accion se realizo correctamente");

            for (int i = 0; i < tabla.getColumnCount(); i++) {
                if (tabla.getColumn(i).getType() == Types.TIME) {
                    tabla.getColumn(i).setType(Types.CHAR);
                }
                if (tabla.getColumn(i).getType() == Types.DATE) {
                    tabla.getColumn(i).setDateFormat("dd/MM/YYYY");
                }
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), ex.getMessage() + "\n",
                    "Error al ejecutar la consulta.", JOptionPane.ERROR_MESSAGE);

        }

    }

}
