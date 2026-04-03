package Hangar.Service;

package Service;

import DAO.ReservationDAO;
import Model.Reservation;

public class FrontDeskService {
    private ReservationDAO resDAO = new ReservationDAO();

    public Reservation validateTailNumber(String tailNum) {
        return resDAO.findByTailNumber(tailNum);
    }

    public boolean performCheckOut(int id) {
        return resDAO.delete(id);
    }
}