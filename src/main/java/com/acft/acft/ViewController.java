package com.acft.acft;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    @GetMapping("/editSoldierData")
    public String serveEditSoldierDataView(){
        return "editSoldierData";
    }
}