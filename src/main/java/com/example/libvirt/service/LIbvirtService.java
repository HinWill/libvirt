package com.example.libvirt.service;

import com.example.libvirt.domain.*;
import com.google.common.collect.Maps;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class LIbvirtService {

    private Libvirt libvirt = new Libvirt();
    private Host host = new Host();

    public LIbvirtService() throws LibvirtException, SigarException {
    }

    /*
        宿主机信息列表
     */
    public Map Hostinf() throws LibvirtException{
        Map information= Maps.newLinkedHashMap();
        int idsOfDomain = libvirt.getConnect().listDomains().length;
        information.put("主机名称",libvirt.getConnect().getHostName());
        information.put("管理IP",libvirt.getConnect().getURI());
        information.put("CPU型号",host.getCpumodel());
        information.put("CPU主频(mhz)",String.valueOf(host.getCpumhz()));
        information.put("最大CPU数",String.valueOf(host.getCpunum()));
        information.put("内存容量(MB)",String.valueOf(host.getMemory()));
        information.put("内存使用率",String.valueOf(host.getMemory()/host.getMemoryused()));
        information.put("实例个数",String.valueOf(idsOfDomain));
        information.put("状态","alive");
        information.put("虚拟化类型",libvirt.getConnect().getType());
        information.put("虚拟化版本",String.valueOf(libvirt.getConnect().getLibVirVersion()));
        return information;
    }

    /*
        存储池信息列表
     */

    public List<Storagepool> Storagepoolist() throws LibvirtException {
        List<Storagepool> poollist = new ArrayList<>();
        String[] pools = libvirt.getConnect().listStoragePools();
        System.out.println("pool have : " + pools.length);
        for (String pool : pools) {
            Storagepool storagepool = new Storagepool(pool);
            poollist.add(storagepool);
        }
        return poollist;
    }
    /*
    网络信息列表
     */
    public List<Net> Netlist() throws SigarException {
        List<Net> netlist = new ArrayList<>();
        Sigar sigar = new Sigar();
        String ifNames[] = sigar.getNetInterfaceList();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            Net net = new Net(name,ifconfig.getAddress(),ifconfig.getType(),String.valueOf(ifconfig.getFlags()),"alive");
            if (ifconfig.getType().equals("Ethernet"))  netlist.add(net);
        }
        return netlist;
    }

    /*
    虚拟机信息列表
     */

    public List<Virtual> Virtuallist() throws LibvirtException {
        List<Virtual> virtuals = new ArrayList<>();
        int[] idsOfDomain = libvirt.getConnect().listDomains();
        for (int i : idsOfDomain) {
            Virtual virtual = new Virtual(i);
            virtuals.add(virtual);
        }
        return virtuals;
    }

    /*
    添加存储池
     */
    public void createStoragepool(String type,String name,String capacity,String url) throws LibvirtException {
        System.out.println(type + " " + name +" " + capacity +" " +url);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<pool type=\""+type+"\">\n" +
                "    <name>"+name+"</name> \n" +
                "    <source>\n" +
                "    </source>\n" +
                "    <capacity unit='GiB'>"+capacity+"</capacity>\n" +
                "    <allocation>0</allocation> \n" +
                "    <target>\n" +
                "        <path>"+url+"</path> \n" +
                "        <permissions> \n" +
                "            <mode>0711</mode>\n" +
                "            <owner>0</owner>\n" +
                "            <group>0</group>\n" +
                "        </permissions>\n" +
                "    </target>\n" +
                "</pool>";
        StoragePool storagePool = libvirt.getConnect().storagePoolCreateXML(xml, 0);
    }


    /*
    通过name删除存储池
     */
    public void deleteStoragePool(String name) throws LibvirtException {
        StoragePool storagePool = libvirt.getConnect().storagePoolLookupByName(name);
        storagePool.destroy();
        storagePool.undefine();
    }

    /*
    通过xml添加虚拟机
     */

    public void createDomain(String xml) throws LibvirtException {
        String xmlDesc = xml;
        Domain domain = libvirt.getConnect().domainCreateXML(xmlDesc, 0);

    }

    /*
    删除虚拟机
     */

    public void deleteDomain(String name) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(name);
        domain.destroy(); // 强制关机
        domain.undefine();
    }

    /*
    暂停/挂起虚拟机
     */

    public void suspendedDomain(String name) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(name);
        domain.suspend();
    }

     /*
        还原暂停/挂起虚拟机
     */

    public void continueDomain(String name) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(name);
        domain.resume();
    }

    /*
        关闭虚拟机
     */

    public void shutdownDomain(String name) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(name);
        domain.shutdown();
    }

    /*
        重启虚拟机
     */
    public void rebootDomain(String name) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(name);
        domain.reboot(0);
    }



    /*
    为虚拟机添加网络
     */

    public void addNet(String domin,String bridge) throws LibvirtException {
        Domain domain = libvirt.getConnect().domainLookupByName(domin);

        String interFace = "<interface type='bridge'>\n" +
                "      <source bridge='"+bridge+"'/>\n" +
                "</interface>\n";
        domain.attachDevice(interFace);
    }



    public static void main(String[] args) throws LibvirtException, SigarException {
        List<Net> poollist= new LIbvirtService().Netlist();

        for (Net s1 : poollist) {
            System.out.println("s " + s1);
        }
    }
}
