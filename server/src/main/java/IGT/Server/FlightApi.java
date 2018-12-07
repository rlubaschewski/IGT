package IGT.Server;

import IGT.Flight.Airport;
import IGT.Flight.Flight;
import IGT.Flight.FlightSegment;
import IGT.Hibernate;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Path("/flight")
public class FlightApi {

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFlight(String json) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date start;
        Long duration;
        JSONArray airportsList;
        int miles;
        try {
            JSONObject object = new JSONObject(json);
            String startString = object.getString("startTime");
            start = df.parse(startString);
            duration = object.getLong("duration");
            airportsList = object.getJSONArray("airportsList");
            miles = object.getInt("miles");
            if (miles < 1 || airportsList.length() < 2) {
                return Responder.badRequest();
            }
            Flight newFlight = new Flight();
            for (int i = 0; i < airportsList.length() - 1; i++) {
                Long currentId = airportsList.getLong(i);
                Long nextId = airportsList.getLong(i + 1);
                Airport currentAirport = Hibernate.getInstance().getElementById(currentId, "Airport");
                Airport nextAirport = Hibernate.getInstance().getElementById(nextId, "Airport");
                FlightSegment nextSegment = new FlightSegment(newFlight, currentAirport, nextAirport);
                newFlight.addFlightSegment(nextSegment);
            }
            newFlight.setStartTime(start);
            newFlight.setDuration(duration);
            newFlight.setMiles(miles);
            Hibernate.getInstance().save(newFlight);
            return Responder.created(newFlight.toJSON());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Responder.exception(e);
        }
    }


    @OPTIONS
    @Path("/new")
    public Response optionsNew() {
        return Responder.preFlight();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFlights() {
        try {
            JSONArray response = new JSONArray();
            Hibernate.getInstance().<Flight>getTable("Flight").forEach(flight -> response.put(flight.toJSON()));
            return Responder.ok(response);
        } catch(Exception e) {
            return Responder.exception(e);
        }
    }


    @POST
    @Path("/filter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilterFlights(String reqString) {
        try {
            JSONObject request = new JSONObject(reqString);
            JSONArray response = new JSONArray();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = request.getString("date");
            Date date = df.parse(dateString);
            List<Flight> flights = Hibernate.getInstance().<Flight>getTable("Flight");
            for (int i = 0; i < flights.size(); i++) {
                if (flights.get(i).getStartTime().equals(date) && flights.get(i).getArrivalTime().equals(date)) {
                    response.put(flights.get(i).toJSON());
                }
            }
            return Responder.ok(response);
        } catch(Exception e) {
            return Responder.exception(e);
        }
    }

}
