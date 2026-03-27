package Service;

import DAO.HangarSlotDAO;
import Model.HangarSlot;
import Util.HangarSlotUtil;
import Util.HangarSlotUtil.ServiceResult;

import java.util.List;
public class HangarSlotService {

    private final HangarSlotDAO dao;

    public HangarSlotService() {
        this.dao = new HangarSlotDAO();
    }

    public ServiceResult<List<HangarSlot>> viewAllHangarsAndSlots() {
        List<HangarSlot> slots = dao.findAll();
        if (slots.isEmpty()) {
            return ServiceResult.failure("No hangars or slots found in the system.");
        }
        return ServiceResult.success(slots);
    }

    public ServiceResult<List<HangarSlot>> viewSlotsByHangar(String hangarName) {
        List<HangarSlot> slots = dao.findByHangar(hangarName);
        if (slots.isEmpty()) {
            return ServiceResult.failure("No slots found for hangar: " + hangarName);
        }
        return ServiceResult.success(slots);
    }

    public ServiceResult<List<HangarSlot>> checkAllSlotAvailability() {
        List<HangarSlot> available = dao.findAllAvailable();
        if (available.isEmpty()) {
            return ServiceResult.failure("No available slots found. All slots are currently occupied.");
        }
        return ServiceResult.success(available);
    }

    public ServiceResult<List<HangarSlot>> checkSlotAvailabilityByHangar(String hangarName) {
        List<HangarSlot> available = dao.findByHangarAndStatus(hangarName, HangarSlot.STATUS_AVAILABLE);
        if (available.isEmpty()) {
            return ServiceResult.failure(
                    "No available slots in hangar: " + hangarName +
                            ". All slots are currently occupied."
            );
        }
        return ServiceResult.success(available);
    }

    public ServiceResult<HangarSlot> checkSlotByCode(String slotCode) {
        HangarSlot slot = dao.findBySlotCode(slotCode);

        if (slot == null) {
            return ServiceResult.failure("Slot '" + slotCode + "' does not exist.");
        }

        if (HangarSlotUtil.isOccupied(slot)) {
            return ServiceResult.failure(
                    "Slot '" + slotCode + "' is currently OCCUPIED."
            );
        }

        return ServiceResult.success(slot);
    }

    public List<HangarSlot> getAllSlots() {
        return dao.findAll();
    }
}