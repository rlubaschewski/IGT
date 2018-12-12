package IGT;

import IGT.Customer.Customer;
import IGT.Customer.Phone;
import IGT.Flight.*;
import org.hibernate.Session;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.List;

public class Hibernate {
    private static Hibernate instance;
    private static EntityManagerFactory emf;

    private Hibernate(){
        System.out.println("use db config: " + Config.DB.name());
        emf = Persistence.createEntityManagerFactory(Config.DB.name());
    }

    public static Hibernate getInstance() {
        if(instance == null){
            instance = new Hibernate();
        }
        return instance;
    }

    public synchronized void initFlightManagement() {
        try {
            PopularAirports.generate();
            RandomFlights.generate(1000);
        } catch (Exception e) {
            System.out.println("initFlightManagement failed");
            e.printStackTrace();
        }
    }

    public synchronized <T extends IClassID> Long save(T object) throws ServerError {
        Session se = emf.createEntityManager().unwrap(Session.class);
        se.beginTransaction();
        try {
            if (object.getId() == null) {
                // save
                se.persist(object);
            } else {
                // update
                se.merge(object);
            }
        } catch (Exception e) {
            throw new ServerError("failed to store: " + object.toJSON().toString(), new Error());
        } finally {
            se.getTransaction().commit();
            se.close();
        }
        return object.getId();
    }


    public synchronized <T extends IClassID> void delete(T object) throws ServerError {
        Session se = emf.createEntityManager().unwrap(Session.class);
        se.beginTransaction();
        try {
            se.delete(se.get(getClass(object), object.getId()));
        } catch (Exception e) {
            throw new ServerError("failed to delete: " + object.toJSON().toString(), new Error());
        } finally {
            se.getTransaction().commit();
            se.close();
        }
    }

    public synchronized <T> List<T> getTable(String table) throws ServerError {
        List<T> customers = new ArrayList<>();
        try {
            for (IClassID c : customQuery("FROM " + table)) {
                customers.add((T) c);
            }
        } catch (Exception e) {
            throw new ServerError("failed to get Table: " + table, new Error());
        }
        return customers;
    }

    public synchronized <T extends IClassID> T getElementById(Object id, String table) throws ServerError {
        List<T> t = getTable(table);
        for (T c : t) {
            if (id.equals(c.getId())) {
                return c;
            }
        }
        System.out.println("cant find element");
        return null;
    }

    public synchronized <T extends IClassID> List<T> customQuery(String query) throws ServerError {
        List<T> customers = new ArrayList<>();
        Session se = emf.createEntityManager().unwrap(Session.class);
        se.beginTransaction();
        try {
            for (Object c : se.createQuery(query).list()) {
                customers.add((T) c);
            }
        } catch (Exception e) {
            throw new ServerError("failed to execute Query: " + query, new Error());
        } finally {
            se.getTransaction().commit();
            se.close();
        }
        return customers;
    }

    private synchronized Class getClass(IClassID classID) {
        switch (classID.getClassId()) {
            case "Customer":
                return Customer.class;
            case "Phone":
                return Phone.class;
            case "Airport":
                return Airport.class;
            case "Flight":
                return Flight.class;
            case "FlightSegment":
                return FlightSegment.class;
        }
        return null;
    }

    public static EntityManagerFactory getFactory() {
        return emf;
    }
}

