package Service;

import DAO.ReservationDAO;
import Model.Reservation;
import Model.FrontDesk;

public class FrontDeskService {
    private final ReservationDAO resDAO = new ReservationDAO();

    public FrontDesk validateTailNumber(String tailNum) {
        if (tailNum == null || tailNum.trim().isEmpty()) return null;
        Reservation res = resDAO.findByTailNumber(tailNum.trim());
        if (res != null) {
            return new FrontDesk.Builder()
                    .id(res.getReservationId())
                    .tail(res.getAircraftTailNumber())
                    .name(res.getCustomerName())
                    .slot(res.getHangarSlot())
                    .start(res.getStartDate().toString())
                    .end(res.getEndDate().toString())
                    .build();
        }
        return null;
    }

    public boolean performCheckOut(int id) {
        return id > 0 && resDAO.delete(id);
    }
}