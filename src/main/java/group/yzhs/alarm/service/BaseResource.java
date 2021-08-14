package group.yzhs.alarm.service;

import group.yzhs.alarm.model.ProductionLine;

import java.util.List;

public interface BaseResource {



    List<ProductionLine> Find();

    /**
     * @param attr CHA_RATE/ALM_LEV/IS_MAIN/LLO_LIM....
     * @param value true 3.0....
     * */
    void Update(String CompanyNo, String DEV_NO, String DEV_TAG_NO, String attr, String value);



    void Delete();


}
