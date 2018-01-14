package com.example.libvirt.controller;


import com.example.libvirt.domain.Net;
import com.example.libvirt.domain.Storagepool;
import com.example.libvirt.domain.Virtual;
import com.example.libvirt.service.LIbvirtService;
import org.hyperic.sigar.SigarException;
import org.libvirt.LibvirtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class LibvirtController {

    @Autowired
    private LIbvirtService lIbvirtService;

    /*
        宿主机信息列表
     */

    @RequestMapping("/")
    public String index(Model model) throws LibvirtException {
        Map hostinfo = lIbvirtService.Hostinf();
        model.addAttribute("hostinfo",hostinfo);
        return "suzhuji";
    }

    /*
    存储池信息列表
     */

    @RequestMapping("/poollist")
    public String poolList(Model model) throws LibvirtException {
        List<Storagepool> poollist = new ArrayList<>();
        poollist = lIbvirtService.Storagepoolist();
        model.addAttribute("poollist",poollist);
        return "cunchu";
    }


    /*
    网络信息列表
     */
    @RequestMapping("/netlist")
    public String netList(Model model) throws SigarException {
        List<Net> netlist = new ArrayList<>();
        netlist = lIbvirtService.Netlist();
        model.addAttribute("netlist",netlist);
        return "wangluo";
    }

    /*
    虚拟机信息列表
     */
    @RequestMapping("/domainlist")
    public String domainList(Model model) throws LibvirtException {
        List<Virtual> domainlist = new ArrayList<>();
        domainlist = lIbvirtService.Virtuallist();
        model.addAttribute("domainlist",domainlist);
        return "xuniji-shili";
    }

    /*
    添加存储池
     */
    @RequestMapping("/addpool")
    public String addPool(Model model, @RequestParam("type") String type,
                          @RequestParam("name") String name,
                          @RequestParam("capacity") String capacity,
                          @RequestParam("url") String url) {
        try {
            lIbvirtService.createStoragepool(type,name,capacity,url);
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
        return "redirect:/poollist";
    }

    /*
    删除存储池
     */
    @RequestMapping("/delpool/{name}")
    public String delPool(Model model,@PathVariable("name") String name) {
        System.out.println("name " + name);
        try {
            lIbvirtService.deleteStoragePool(name);
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
        return "redirect:/poollist";
    }

    @RequestMapping("/adddomain")
    public String addMoain(){
        return "xuniji-chuangjian";
    }

    /*
    通过xml添加虚拟机
     */
    @RequestMapping("/addomain")
    public String addoMain(Model model,@RequestParam("xml")String xml) throws LibvirtException {
        lIbvirtService.createDomain(xml);
        return "forward:/domainlist";
    }

    /*
    暂停/挂起虚拟机
     */
    @RequestMapping("/supdomain/{name}")
    public String supdoMain(Model model,@PathVariable("name") String name) throws LibvirtException {
        lIbvirtService.suspendedDomain(name);
        return "redirect:/domainlist";
    }
    /*
    还原暂停/挂起虚拟机
     */
    @RequestMapping("/condomain/{name}")
    public String condoMain(Model model,@PathVariable("name") String name) throws LibvirtException {
        lIbvirtService.continueDomain(name);
        return "redirect:/domainlist";
    }

    /*
    关闭虚拟机
     */
    @RequestMapping("/shutdomain/{name}")
    public String shutdoMain(Model model,@PathVariable("name") String name) throws LibvirtException {
        lIbvirtService.shutdownDomain(name);
        return "redirect:/domainlist";
    }

    /*
    删除虚拟机
     */
    @RequestMapping("/deldomain/{name}")
    public String deldoMain(Model model,@PathVariable("name") String name) throws LibvirtException {
        lIbvirtService.deleteDomain(name);
        return "redirect:/domainlist";
    }

    /*
    重启虚拟机
     */
    @RequestMapping("/rebdomain/{name}")
    public String rebdoMain(Model model,@PathVariable("name") String name) throws LibvirtException {
        lIbvirtService.rebootDomain(name);
        return "redirect:/domainlist";
    }

    /*
    为虚拟机添加网络
     */
    @RequestMapping("/addnet/{domain}/{bridge}")
    public String addNet(Model model,
                         @PathVariable("domain") String domain,
                         @PathVariable("bridge") String bridge) throws LibvirtException {
        lIbvirtService.addNet(domain,bridge);
        return "wangluo";
    }

}
