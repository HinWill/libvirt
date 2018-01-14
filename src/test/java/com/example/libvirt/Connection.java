package com.example.libvirt;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hyperic.sigar.*;
import org.libvirt.*;

import java.io.*;


public class Connection {
    private Connect connect;

    public Connection() throws LibvirtException {
        connect = new Connect("qemu:///system",false);
    }

    public void remoteConnectByTcp() throws LibvirtException {
        System.out.println("连接宿主机的名字：" + connect.getHostName());
        System.out.println("JNI连接的libvirt库版本号：" + connect.getLibVirVersion());
        System.out.println("连接的URI：" + connect.getURI());
        System.out.println("连接到的宿主机的剩余内存："+ connect.getFreeMemory());
        System.out.println("连接到的宿主机的最大Cpu输了："+ connect.getMaxVcpus(null));
        System.out.println("hypervisor的名称："+connect.getType());
    }

    //查看存储池列表以及获取存储池的信息
    public void listStroagePool() throws LibvirtException {
        String[] pools = connect.listStoragePools();
        System.out.println("pool have : " + pools.length);
        for (String pool : pools) {
            System.out.println("pool names is :" + pool);
            StoragePool storagePool = connect.storagePoolLookupByName(pool);
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();

            System.out.println("存储池的状态：" + storagePoolInfo.state);
            System.out.println("存储池的容量：GB" + storagePoolInfo.capacity / 1024.00 / 1024.00 / 1024.00);
            System.out.println("存储池的可用容量：GB" + storagePoolInfo.available / 1024.00 / 1024.00 / 1024.00);
            System.out.println("存储池的已用容量：GB" + storagePoolInfo.allocation / 1024.00 / 1024.00 / 1024.00);
            System.out.println("存储池的描述xml：" + storagePool.getXMLDesc(0));
        }
    }

    //存储池的创建
    public void createStoragePool() throws LibvirtException, DocumentException {
        SAXReader reader = new SAXReader ();
        Document document = reader.read(new File("/home/will/xml/kvmdemo-storage-pool.xml"));
        String xmlDesc = document.asXML();

        StoragePool storagePool = connect.storagePoolCreateXML(xmlDesc, 0);
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();

        System.out.println("存储池的状态：" + storagePoolInfo.state);
        System.out.println("存储池的容量：" + storagePoolInfo.capacity / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储池的可用容量：" + storagePoolInfo.available / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储池的已用容量：" + storagePoolInfo.allocation / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储池的描述xml：" + storagePool.getXMLDesc(0));

    }
    //存储池的删除
    public void deleteStoragePool(String name) throws LibvirtException, DocumentException{
        StoragePool storagePool = connect.storagePoolLookupByName(name);

        System.out.println("要删除的存储池为：" + name);
        //storagePool.free();
        storagePool.destroy();
        storagePool.undefine();
    }

    //查询存储卷列表
    public void listStorageVolume(String name) throws LibvirtException{
        StoragePool storagePool = connect.storagePoolLookupByName(name);

        String[] volumes = storagePool.listVolumes();

        System.out.println("the volumes have :" + volumes.length);

        for (String volume : volumes) {
            if (volume.contains("iso")) continue;
            System.out.println("volume name is :" + volume);
        }
    }

    //创建存储卷
    public void createStorageVolume() throws LibvirtException, DocumentException{

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("/home/will/xml/kvmdemo-storage-vol.xml"));
        String xmlDesc = document.asXML();
        System.out.println("createStorageVolume description:" + xmlDesc);

        StoragePool storagePool = connect.storagePoolLookupByName("defaults");
        System.out.println("This could take some times at least 3min...");
        StorageVol storageVol = storagePool.storageVolCreateXML(xmlDesc, 0);
        System.out.println("create success");
        System.out.println("createStorageVolume name:{}" + storageVol.getName());
        System.out.println("createStorageVolume path:{}" + storageVol.getPath());
        StorageVolInfo storageVolInfo = storageVol.getInfo();

        System.out.println("存储卷的类型：" +  storageVolInfo.type);
        System.out.println("存储卷的容量： GB" + storageVolInfo.capacity / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的可用容量： GB" + (storageVolInfo.capacity - storageVolInfo.allocation) / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的已用容量： GB" + storageVolInfo.allocation / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的描述xml： " + storageVol.getXMLDesc(0));
    }

    //克隆存储卷
    public void cloneStorageVolume() throws LibvirtException, DocumentException {


        // xml 文件 => Dom4j 文档 => String
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("/home/will/xml/kvmdemo-storage-vol.xml"));
        String xmlDesc = document.asXML();
        System.out.println("createStorageVolume description:" +  xmlDesc);

        StoragePool storagePool = connect.storagePoolLookupByName("defaults");
        // 克隆的基镜像，这个镜像需要自己制作，可使用 virt-manager 制作基镜像，本示例代码采用的基镜像是 Ubuntu 16.04 64位
        StorageVol genericVol = storagePool.storageVolLookupByName("generic.qcow2");
        System.out.println("This could take some times at least 3min...");
        StorageVol storageVol = storagePool.storageVolCreateXMLFrom(xmlDesc, genericVol, 0);
        System.out.println("clone success");
        System.out.println("createStorageVolume name:" + storageVol.getName());
        System.out.println("createStorageVolume path:" + storageVol.getPath());
        StorageVolInfo storageVolInfo = storageVol.getInfo();

        System.out.println("存储卷的类型：" + storageVolInfo.type);
        System.out.println("存储卷的容量： GB" + storageVolInfo.capacity / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的可用容量： GB" + (storageVolInfo.capacity - storageVolInfo.allocation) / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的已用容量： GB" + storageVolInfo.allocation / 1024.00 / 1024.00 / 1024.00);
        System.out.println("存储卷的描述xml：" + storageVol.getXMLDesc(0));
    }

    //删除存储卷
    public void deleteStorageVolume() throws LibvirtException, DocumentException {

        StoragePool storagePool = connect.storagePoolLookupByName("defaults");
        StorageVol storageVol = storagePool.storageVolLookupByName("kvmdemo.qcow2");
        System.out.println("存储卷名称：" + storageVol.getName());
        storageVol.wipe();
        storageVol.delete(0);
    }

    //写入网桥信息
    public  void modifynetwork() throws IOException {

        FileWriter writer = null;
        String fileName = "/etc/network/interfaces";
        String content = "auto ens33\n" +
                "iface ens33 inet manual\n" +
                "auto br0\n" +
                "iface br0 inet static\n" +
                "address 192.168.136.231\n" +
                "netmask 255.255.255.0\n" +
                "broadcast 192.168.136.255\n" +
                "gateway 192.168.136.2\n" +
                "dns-nameserver 192.168.136.1\n" +
                "bridge_ports ens33\n" +
                "bridge_stp off\n" +
                "bridge_fd 0\n" +
                "bridge_maxwait 0";

        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取网卡信息
    private static void net() throws Exception {
        Sigar sigar = new Sigar();
        String ifNames[] = sigar.getNetInterfaceList();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            System.out.println("网络设备名:    " + name);// 网络设备名
            System.out.println("IP地址:" + ifconfig.getAddress());// IP地址
            System.out.println("网关广播地址:" + ifconfig.getBroadcast());// 网关广播地址
            System.out.println("网卡MAC地址:" + ifconfig.getHwaddr());// 网卡MAC地址
            System.out.println("子网掩码:" + ifconfig.getNetmask());// 子网掩码
            System.out.println("网卡描述信息:" + ifconfig.getDescription());// 网卡描述信息
            System.out.println("网卡类型" + ifconfig.getType());//
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                System.out.println("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }
            System.out.println("--------------------------------------------------------------------");
        }
    }

    //遍历虚拟机信息
    public void listDomain() throws LibvirtException {

        int[] idsOfDomain = connect.listDomains();
        System.out.println("正在运行的虚拟机个数：" + idsOfDomain.length);
        for (int id : idsOfDomain) {
            System.out.println("正在运行的id：" + id);
            Domain domain = connect.domainLookupByID(id);
            System.out.println("虚拟机的id：" + domain.getID());
            System.out.println("虚拟机的uuid：" + domain.getUUIDString());
            System.out.println("虚拟机的名称：" + domain.getName());
            System.out.println("虚拟机的是否自动启动：" + domain.getAutostart());
            System.out.println("虚拟机的状态：" + domain.getInfo().state);
            System.out.println("virsh type:" + domain.getOSType());
            System.out.println("virsh memory:" + domain.getMaxMemory());
            System.out.println("virsh xml:" + domain.getXMLDesc(0));
            int begain = domain.getXMLDesc(0).indexOf("<mac address=");
            String mac = domain.getXMLDesc(0).substring(begain+14,begain+31);
            System.out.println("mac = " + mac);
        }

        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        System.out.println("已定义未运行的虚拟机个数：" + namesOfDefinedDomain.length);
        for (String name : namesOfDefinedDomain) {
            System.out.println("已定义未运行的虚拟机名称：" + name);
            Domain domain = connect.domainLookupByName(name);
            System.out.println("虚拟机的id：" + domain.getID());
            System.out.println("虚拟机的uuid：" + domain.getUUIDString());
            System.out.println("虚拟机的名称：" + domain.getName());
            System.out.println("虚拟机的是否自动启动：" + domain.getAutostart());
            System.out.println("虚拟机的状态：" + domain.getInfo().state);
        }
    }

    //通过xml创建虚拟机
    public void createDomain() throws LibvirtException, DocumentException {

        // xml 文件 => Dom4j 文档 => String
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("/home/will/xml/demo.xml"));
        String xmlDesc = document.asXML();
        System.out.println("createDomain description: " + xmlDesc);

        Domain domain = connect.domainCreateXML(xmlDesc, 0);

        System.out.println("虚拟机的id：" + domain.getID());
        System.out.println("虚拟机的uuid：" + domain.getUUIDString());
        System.out.println("虚拟机的名称： " + domain.getName());
        System.out.println("虚拟机的是否自动启动：" + domain.getAutostart());
        System.out.println("虚拟机的状态：" + domain.getInfo().state);
        System.out.println("虚拟机的描述xml：" + domain.getXMLDesc(0));
    }

    //删除虚拟机
    public void undefineDomain() throws LibvirtException, DocumentException {

        Domain domain = connect.domainLookupByID(4);
        System.out.println("虚拟机的id：" + domain.getID());
        System.out.println("虚拟机的uuid：" + domain.getUUIDString());
        System.out.println("虚拟机的名称：" + domain.getName());
        System.out.println("虚拟机的是否自动启动：" + domain.getAutostart());
        System.out.println("虚拟机的状态：" + domain.getInfo().state);
        domain.destroy(); // 强制关机
        domain.undefine();
    }

    public void test(){
        try {
            String[] cmd = new String[] { "/home/will/noVNC/utils/launch.sh", "--vnc", "192.168.136.136:5902"};
            Process ps = Runtime.getRuntime().exec(cmd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Connection conn = new Connection();
        conn.test();
    }
}
