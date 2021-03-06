package VentanaInspector;

import VentanaInspector.ManejoFechas.Fechas;
import javafx.util.Pair;
import quick.dbtable.DBTable;

import javax.swing.*;
import java.sql.*;
import java.util.Date;
import java.util.LinkedList;

public class InspectorLogica {



    private DefaultListModel<String> listaPatentesModel;



    private LinkedList<String> listaPatentesMulta;
    private Inspector inspector;
    private DBTable tabla;
    private Date diaMulta;
    private Time horaMulta;


    public InspectorLogica(Inspector inspector,DBTable tabla){
        this.inspector=inspector;
        this.tabla=tabla;
        listaPatentesMulta =new LinkedList<String>();
        listaPatentesModel=new DefaultListModel<String>();
    }

    /**
     *Agrega las patentes a una lista y a un model de lista
     * @param patente
     */
    public void agregarPatente(String patente){
        listaPatentesModel.addElement(patente);
        listaPatentesMulta.add(patente);
    }


    /**
     * elimina las patentes que no van a ser multadas
     * @param ubicacion
     * @param altura
     */
    private void patentesAMultar(String ubicacion,int  altura){
        try {
            Connection conexion=tabla.getConnection();
            Statement statement= conexion.createStatement();
            String alturaS=String.valueOf(altura);
            ResultSet rs=statement.executeQuery("SELECT patente FROM estacionados WHERE calle='"+ubicacion+"' and altura="+altura+";");
            while(rs.next()){
                String patente=rs.getString("patente");
                listaPatentesMulta.remove(patente);
                listaPatentesModel.removeElement(patente);
            }

        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Verifica si el inspector puede realizar la multa en esa ubicacion
     * Controla la lista de patentes multar y las agrega a la BD Multas
     * @param ubicacion
     * @param altura
     */
    public boolean agregarMultas(String ubicacion, int altura) throws SQLException {
        try {
            patentesAMultar(ubicacion,altura);
            System.out.println(listaPatentesMulta);
            inspector.setAlturaSeleccionado(altura);
            inspector.setCalleSeleccionado(ubicacion);

            Connection conexion=tabla.getConnection();
            Statement statement= conexion.createStatement();
            ResultSet rsFecha= statement.executeQuery("SELECT CURDATE(),CURTIME();");

            if(rsFecha.next()) {
                diaMulta = rsFecha.getDate("CURDATE()");
                horaMulta = rsFecha.getTime("CURTIME()");
            }


            Pair<String,String> diaTurno=obtenerDiaTurno(diaMulta,horaMulta);
            int id_asociado_con=inspector.getID(tabla,diaTurno.getKey(),diaTurno.getValue());

            if(id_asociado_con==-1){
                return false;
            }
            for(String p: listaPatentesMulta) {
                int rs = statement.executeUpdate("INSERT INTO multa(fecha,hora,patente,id_asociado_con) VALUE ('"+diaMulta+ "','" +horaMulta+ "','"+p+"',"+id_asociado_con+");");
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * Obtiene el dia y el turno actual
     * @param dia
     * @param hora
     * @return
     * @throws Exception
     */
    private Pair<String,String> obtenerDiaTurno(Date dia, Time hora) throws Exception{
        String sDia=null;
        Date entradaManiana= Fechas.convertirStringATime("08:00:00");
        Date salidaManiana= Fechas.convertirStringATime("13:59:00");
        Date entradaTarde= Fechas.convertirStringATime("14:00:00");
        Date salidaTarde= Fechas.convertirStringATime("20:00:00");
        String turno=null;

        try {

            Connection conexion=tabla.getConnection();
            Statement statement= conexion.createStatement();
            ResultSet rs= statement.executeQuery("SELECT DAYOFWEEK('"+dia+"');");
            if(rs.next()) {
                int nDia=rs.getInt("DAYOFWEEK('"+dia+"')");
                switch (nDia){
                    case 1:sDia="do";break;
                    case 2:sDia="lu";break;
                    case 3:sDia="ma";break;
                    case 4:sDia="mi";break;
                    case 5:sDia="ju";break;
                    case 6:sDia="vi";break;
                    case 7:sDia="sa";break;
                }
            }
            if(hora.after(entradaManiana) && hora.before(salidaManiana)){
                turno="m";
            }
            else if(hora.after(entradaTarde) && hora.before(salidaTarde)){
                turno="t";
            }
            else throw new Exception("Fuera de horario de trabajo");

        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new Pair<String,String>(sDia,turno);
    }

    public void marcarAcceso(int id_parq) throws SQLException {
        //Obtengo el id del parquimetro y registro el acceso del inspector al parquimetro
        Connection conexion=tabla.getConnection();
        Statement statement= conexion.createStatement();
        statement.executeUpdate("INSERT INTO accede(legajo,id_parq,fecha,hora) VALUE ('"+inspector.getLegajo()+"','"+id_parq+"','"+diaMulta+"','"+horaMulta+"');");
    }

    public Date getDiaMulta() {
        return diaMulta;
    }


    public Time getHoraMulta() {
        return horaMulta;
    }

    public DefaultListModel<String> getListaPatentesModel() {
        return listaPatentesModel;
    }

    public void setListaPatentesModel(DefaultListModel<String> listaPatentesModel) {
        this.listaPatentesModel = listaPatentesModel;
    }

    public LinkedList<String> getListaPatentesMulta() {
        return listaPatentesMulta;
    }

    public void setListaPatentesMulta(LinkedList<String> listaPatentesMulta) {
        this.listaPatentesMulta = listaPatentesMulta;
    }

}

