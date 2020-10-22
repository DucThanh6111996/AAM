package com.viettel.it.util;

import com.google.common.net.InetAddresses;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class IpUtil {
	static Logger logger = Logger.getLogger(IpUtil.class);
	static Map<String,List<String>> mapCidr2listAddr = new HashMap<>();
	private static Comparator<? super String> ipComparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return (int) (ipToLong(o1)-ipToLong(o2));	
		}
	};

	public static boolean isIpAddress(String ip) {
		if (ip == null || ip.isEmpty())
			return false;
		return InetAddresses.isInetAddress(ip);
	}

	public static boolean isRangeIpAddress(String ipL, String ipH) {
		try {
			if (ipL == null || ipL.isEmpty())
				return false;
			if(!InetAddresses.isInetAddress(ipL)) return false;
			if(!InetAddresses.isInetAddress(ipH)) return false;
			long ipLo = ipToLong(InetAddress.getByName(ipL));
			long ipHi = ipToLong(InetAddress.getByName(ipL.replaceAll("\\d{1,3}$", "255")));
			if (ipH == null || ipH.isEmpty())
				return false;
			long ipToTest = ipToLong(InetAddress.getByName(ipH));
			return (ipToTest >= ipLo && ipToTest <= ipHi);
		} catch (UnknownHostException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkIpInRange(String ipSubnet, String ip) {
		SubnetUtils utils;
		try {
			utils = new MySubnetUtils(ipSubnet).getSubnetUtils();
			//System.err.println(utils.getInfo().getLowAddress());
			//System.err.println(utils.getInfo().getHighAddress());
			if(utils.getInfo().isInRange(ip))
				return true;
			if(utils.getInfo().getBroadcastAddress().equalsIgnoreCase(ip))
				return true;
			if(utils.getInfo().getNetworkAddress().equalsIgnoreCase(ip))
				return true;
			//return cidr2listAddr(ipSubnet).contains(ip);
			//return utils.getInfo().isInRange(ip);
		} catch (Exception e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkIpInRange(String ipL, String ipH, String ip) {
		try {
			if(!InetAddresses.isInetAddress(ipL)) return false;
			if(!InetAddresses.isInetAddress(ipH)) return false;
			if(!InetAddresses.isInetAddress(ip)) return false;
			long ipLo = ipToLong(InetAddress.getByName(ipL));
			long ipHi = ipToLong(InetAddress.getByName(ipH));
			long ipToTest = ipToLong(InetAddress.getByName(ip));
			return (ipToTest >= ipLo && ipToTest <= ipHi);
		} catch (UnknownHostException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkSubRangeIps(String ips, String ipsSub) {
		if (ips.contains(":") || ips.contains("-")) {
			String[] _ips = ips.split("[:-]", -1);
			if (ipsSub.contains(":") || ipsSub.contains("-")) {
				String[] _ipsSub = ipsSub.split("[:-]", -1);
				return checkSubRangeIp(_ips[0], _ips[1], _ipsSub[0], _ipsSub[1]);
			} else if (ipsSub.contains("/")) {
				return checkSubRangeIp(_ips[0], _ips[1], ipsSub, true);
			} else {
				return checkIpInRange(_ips[0], _ips[1], ipsSub);
			}
		} else if (ips.contains("/")) {
			if (ipsSub.contains(":")||ipsSub.contains("-")) {
				String[] _ipsSub = ipsSub.split("[:-]", -1);
				return checkSubRangeIp(ips, _ipsSub[0], _ipsSub[1]);
			} else if (ipsSub.contains("/")) {
				return checkSubRangeIp(ips, ipsSub);
			} else {
				return checkIpInRange(ips, ipsSub);
			}
		} else {
			return checkSubRangeIps(ips+"/32", ipsSub);
		}

	}
	public static boolean checkSubRangeIp(String ipSubnet, String ipSubnetSub) {
		try {
			SubnetUtils utils = new MySubnetUtils(ipSubnet).getSubnetUtils();
			SubnetUtils utilSubs = new MySubnetUtils(ipSubnetSub).getSubnetUtils();
			if("0.0.0.0/0".equals(ipSubnet))
				return true;
			if("0.0.0.0/0".equals(ipSubnetSub) && "0.0.0.0/0".equals(ipSubnet))
				return true;
			if("0.0.0.0/0".equals(ipSubnetSub) && !"0.0.0.0/0".equals(ipSubnet))
				return false;
			String lowAddress = utils.getInfo().getLowAddress();
			String highAddress = utils.getInfo().getHighAddress();
			if(utils.getInfo().getCidrSignature().endsWith("/32")){
				highAddress = lowAddress = utils.getInfo().getNetworkAddress();
			}else if(utils.getInfo().getCidrSignature().endsWith("/31")){
				lowAddress = utils.getInfo().getNetworkAddress();
				highAddress = utils.getInfo().getBroadcastAddress();
			}
			
			String lowAddress2 = utilSubs.getInfo().getLowAddress();
			String highAddress2 = utilSubs.getInfo().getHighAddress();
			if(utilSubs.getInfo().getCidrSignature().endsWith("/32")){
				highAddress2 = lowAddress2 = utilSubs.getInfo().getNetworkAddress();
			}else if(utilSubs.getInfo().getCidrSignature().endsWith("/31")){
				lowAddress2 = utilSubs.getInfo().getNetworkAddress();
				highAddress2 = utilSubs.getInfo().getBroadcastAddress();
			}
			return checkSubRangeIp(lowAddress, highAddress, lowAddress2, highAddress2);
		} catch (Exception e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkSubRangeIp(String ipSubnet, String ipLSub, String ipHSub) {
		try {
			SubnetUtils utils = new MySubnetUtils(ipSubnet).getSubnetUtils();
			return checkSubRangeIp(utils.getInfo().getLowAddress(), utils.getInfo().getHighAddress(), ipLSub, ipHSub);
		} catch (Exception e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkSubRangeIp(String ipL, String ipH, String ipSubSubnet, boolean unused) {
		try {
			if(!InetAddresses.isInetAddress(ipL)) return false;
			if(!InetAddresses.isInetAddress(ipH)) return false;
			long ipLo = ipToLong(InetAddress.getByName(ipL));
			long ipHi = ipToLong(InetAddress.getByName(ipH));
			SubnetUtils utils = new MySubnetUtils(ipSubSubnet).getSubnetUtils();
			long ipLoSub = ipToLong(InetAddress.getByName(utils.getInfo().getLowAddress()));
			long ipHiSub = ipToLong(InetAddress.getByName(utils.getInfo().getHighAddress()));
			return ipLo <= ipLoSub && ipHi >= ipHiSub;
		} catch (UnknownHostException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkSubRangeIp(String ipL, String ipH, String ipLSub, String ipHSub) {
		try {
			if(!InetAddresses.isInetAddress(ipL)) return false;
			if(!InetAddresses.isInetAddress(ipH)) return false;
			if(!InetAddresses.isInetAddress(ipLSub)) return false;
			if(!InetAddresses.isInetAddress(ipHSub)) return false;
			long ipLo = ipToLong(InetAddress.getByName(ipL));
			long ipHi = ipToLong(InetAddress.getByName(ipH));
			long ipLoSub = ipToLong(InetAddress.getByName(ipLSub));
			long ipHiSub = ipToLong(InetAddress.getByName(ipHSub));
			return ipLo <= ipLoSub && ipHi >= ipHiSub;
		} catch (UnknownHostException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean checkSubRangePort(String port, String portSub) {
		if(port==null || portSub ==null)
			return false;
		if(portSub.equals(port))
			return true;
		String[] ports = port.split(",", -1);
		String[] portSubs = portSub.split(",", -1);
		int nMatch = 0;
		for (String ps : portSubs) {
			for (String p : ports) {
				if (checkContainPort(p, ps)){
					nMatch++;
					break;
				}
			}
		}
		return nMatch == portSubs.length;
	}
	private static boolean checkContainPort(String port, String portSub) {
		try {
			if(!isPortOrRangePort(port))
				return false;
			if(!isPortOrRangePort(portSub))
				return false;
			if (port.contains(":") || port.contains("-")) {
				int portLo = Integer.parseInt(port.split("[:-]", -1)[0]);
				int portHi = Integer.parseInt(port.split("[:-]", -1)[1]);
				if (portSub.contains(":")||portSub.contains("-")) {
					int portSubLo = Integer.parseInt(portSub.split("[:-]", -1)[0]);
					int portSubHi = Integer.parseInt(portSub.split("[:-]", -1)[1]);
					return portLo <= portSubLo && portSubHi <= portHi;
				} else {
					return portLo <= Integer.parseInt(portSub) && portHi >= Integer.parseInt(portSub);
				}
			} else {
				if (portSub.contains(":") || portSub.contains("-")) {
					return false;
				} else {
					return Integer.parseInt(port) == Integer.parseInt(portSub);
				}
			}
		} catch (NumberFormatException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean isSubnetAddress(String subnet) {
		// SubnetUtils utils;
		try {
			new MySubnetUtils(subnet).getSubnetUtils();
			// System.err.println(utils.getInfo().getLowAddress());
			// System.err.println(utils.getInfo().getHighAddress());
			return true;
		} catch (Exception e) {
			logger.warn(e);
		}

		return false;
	}

	public static boolean isPortOrRangePort(String port) {
		if (port == null || port.isEmpty())
			return false;
		String[] ports = port.split(",", -1);

		try {
			for (String p : ports) {
				if (p.contains(":") || p.contains("-")) {
					String[] _ps = p.split("[:-]", -1);
					if (_ps.length == 2) {
						if (Integer.parseInt(_ps[0]) < 1 || Integer.parseInt(_ps[0]) > 65535)
							return false;
						if (Integer.parseInt(_ps[1]) < 1 || Integer.parseInt(_ps[1]) > 65535)
							return false;
						if (Integer.parseInt(_ps[0]) > Integer.parseInt(_ps[1]))
							return false;
					} else
						return false;

				} else if (Integer.parseInt(p) < 1 || Integer.parseInt(p) > 65535)
					return false;
			}
			return true;
		} catch (NumberFormatException e) {
			logger.warn(e);
		}
		return false;
	}
	public static boolean isTcpOrUdp(String protocol) {
		if (protocol == null)
			return false;
		if (protocol.contains("/") || protocol.contains("\\")) {
			String[] ps = protocol.split("/|\\\\", -1);
			if (ps.length == 2) {
				if (isNotProtocol(ps[0])) {
					return false;
				}
				if (isNotProtocol(ps[1])) {
					return false;
				}
			} else {
				return false;
			}
		} else {
			if (isNotProtocol(protocol)) {
				return false;
			}
		}

		return true;
	}
	private static boolean isNotProtocol(String protocol){
		return (!"tcp".equalsIgnoreCase(protocol) && !"udp".equalsIgnoreCase(protocol) && !"sctp".equalsIgnoreCase(protocol));
		
	}

	static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;

	}
	public static List<String> range2AllAddr(String startIp, String endIp){
		Set<String> ips = new HashSet<>();
		for(String cidr:range2cidrlist(startIp, endIp)){
			ips.addAll(cidr2listAddr(cidr));
		}
		ArrayList<String> arrayList = new ArrayList<String>(ips);
		Collections.sort(arrayList , ipComparator);
		return arrayList;
	}
	public static List<String> cidr2listAddr (String cidr){
		if(mapCidr2listAddr.containsKey(cidr))
			return mapCidr2listAddr.get(cidr);
		Set<String> ips = new HashSet<>();
		SubnetUtils utils = new MySubnetUtils(cidr).getSubnetUtils();
		ips.add(utils.getInfo().getBroadcastAddress());
		ips.add(utils.getInfo().getNetworkAddress());
		ips.addAll(Arrays.asList(utils.getInfo().getAllAddresses()));
		ArrayList<String> arrayList = new ArrayList<String>(ips);
		Collections.sort(arrayList,ipComparator );
		mapCidr2listAddr.put(cidr, arrayList);
		return arrayList;
	}
	public static List<String> cidr2listAddrOctet4div4 (String cidr){
		return cidr2listAddrOctet4divX(cidr, 4);
	}
	
	public static List<String> cidr2listAddrOctet4div16 (String cidr){
		return cidr2listAddrOctet4divX(cidr, 16);
	}
	public static List<String> cidr2listAddrOctet4div8 (String cidr){
		return cidr2listAddrOctet4divX(cidr, 8);
	}
	public static List<String> cidr2listAddrOctet4divX (String cidr, int x){
		List<String> list =cidr2listAddr(cidr);
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String strIP = iterator.next();
			 String[] ipSec = strIP.split("\\.");         
		        if(Integer.valueOf(ipSec[3])%x!=0){
		        	iterator.remove();
		        }
		}
		return list;
	}
    public static List<String> range2cidrlist( String startIp, String endIp ) {         
        long start = ipToLong(startIp);         
        long end = ipToLong(endIp);           

        List<String> pairs = new ArrayList<String>();         
        while ( end >= start ) {             
            byte maxsize = 32;             
            while ( maxsize > 0) {                 
                long mask = CIDR2MASK[ maxsize -1 ];                 
                long maskedBase = start & mask;                 

                if ( maskedBase != start ) {                     
                    break;                 
                }                 

                maxsize--;             
            }               
            double x = Math.log( end - start + 1) / Math.log( 2 );             
            byte maxdiff = (byte)( 32 - Math.floor( x ) );             
            if ( maxsize < maxdiff) {                 
                maxsize = maxdiff;             
            }             
            String ip = longToIP(start);             
            pairs.add( ip + "/" + maxsize);             
            start += Math.pow( 2, (32 - maxsize) );         
        }         
        return pairs;     
    }       

    protected static final int[] CIDR2MASK = new int[] { 0x00000000, 0x80000000,
        0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000,             
        0xFE000000, 0xFF000000, 0xFF800000, 0xFFC00000, 0xFFE00000,             
        0xFFF00000, 0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000,             
        0xFFFF8000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFF800,             
        0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00, 0xFFFFFF80, 0xFFFFFFC0,             
        0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE,             
        0xFFFFFFFF };       

    private static long ipToLong(String strIP) {         
        long[] ip = new long[4];         
        String[] ipSec = strIP.split("\\.");         
        for (int k = 0; k < 4; k++) {             
            ip[k] = Long.valueOf(ipSec[k]);         
        }         

        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];     
    }       

    private static String longToIP(long longIP) {         
        StringBuffer sb = new StringBuffer("");         
        sb.append(String.valueOf(longIP >>> 24));         
        sb.append(".");         
        sb.append(String.valueOf((longIP & 0x00FFFFFF) >>> 16));         
        sb.append(".");         
        sb.append(String.valueOf((longIP & 0x0000FFFF) >>> 8));         
        sb.append(".");         
        sb.append(String.valueOf(longIP & 0x000000FF));   

        return sb.toString();     
    } 
    public static String plusIpOctet2(String ip, int num){
    	if(ip!=null){
    		String[] _ips = ip.split("\\.", -1);
    		StringBuffer sb = new StringBuffer("");
    		sb.append(_ips[0]);
    		sb.append(".");
    		sb.append(Integer.valueOf(_ips[1])+num);
    		sb.append(".");
    		sb.append(_ips[2]);
    		sb.append(".");
    		sb.append((_ips[3]));
    		if(isIpAddress(sb.toString()))
    			return sb.toString();
    	}
    		
    	return null;
    }
    public static String plusIp(String ip, int num){
    	if(ip!=null){
    		String[] _ips = ip.split("\\.", -1);
    		StringBuffer sb = new StringBuffer("");
    		sb.append(_ips[0]);
    		sb.append(".");
    		sb.append(_ips[1]);
    		sb.append(".");
    		sb.append(_ips[2]);
    		sb.append(".");
    		sb.append(Integer.valueOf(_ips[3])+num);
    		if(isIpAddress(sb.toString()))
    			return sb.toString();
    	}
    		
    	return null;
    }

	public static void main(String[] args) {
//		System.err.println(IpUtil.plusIp("10.25.36.251", 5));;
		System.err.println(IpUtil.cidr2listAddr("100.64.0.0/16").size());
		//System.err.println(IpUtil.range2cidrlist("10.172.0.0", "10.172.127.255"));
//		SubnetUtils utils = new MySubnetUtils("192.168.1.3/31").getSubnetUtils();
//		System.err.println(utils.getInfo().getHighAddress());
//		System.err.println(utils.getInfo().getLowAddress());
//		System.err.println(utils.getInfo().getNetworkAddress());
//		System.err.println(utils.getInfo().getBroadcastAddress());
//		System.out.println(checkSubRangeIps("192.168.1.1/32", "192.168.1.2/31"));
//		
//		System.err.println(cidr2listAddr("192.168.1.1/32"));
//		System.err.println(cidr2listAddr("192.168.1.2/31"));
//		System.err.println(IpUtil.isIpAddress("10.61.96.*"));
//		System.err.println(IpUtil.checkIpInRange("0.0.0.0/0", "10.61.94.101"));
//		System.err.println(IpUtil.checkSubRangeIps("10.61.94.101/25", "10.61.94.101/20"));
//		System.err.println(IpUtil.checkSubRangeIps("10.61.94.101:10.61.94.201", "10.61.94.101/30"));
//		System.err.println(IpUtil.checkSubRangeIps("10.61.94.101:10.61.94.201", "10.61.94.103:10.61.94.106"));
//		System.err.println(IpUtil.isPortOrRangePort("21,22,23,6000:10000,-90"));
//		System.err.println();
//		System.err.println(IpUtil.checkSubRangePort("21,22,23,6000:10000", "21,22,23,6000:10000"));
//		System.err.println(IpUtil.checkSubRangePort("21,22,23,6000:10000", "21,22,6000:10000"));
//		System.err.println(IpUtil.checkSubRangePort("21,22,23,6000:10000", "21,22,24,6000:10000"));
//		System.err.println(IpUtil.checkSubRangePort("21,22,23,6000:10000", "21,22,23,7000:80000"));
//		System.err.println(IpUtil.checkSubRangePort("21,22,23,6000:10000", "21,22,23,8000:9000"));
//		
//		List<String> range2cidrlist = range2AllAddr("10.61.94.70", "10.61.94.75");
//		range2cidrlist = cidr2listAddr("");
//		
//		//range2cidrlist = range2AllAddr("10.61.151.0", "255.255.255.0");
//		boolean a = checkIpInRange("10.61.151.0/24", "10.61.151.60");
//		System.err.println(a);
//		System.err.println(range2cidrlist);
//		for(int i=0;i<=32;i++){
//			String a = i + ": " + new SubnetUtils("10.61.151.0/"+ i).getInfo().getNetmask();
//			System.err.println(a);
//		}
		
//		for(String r : range2cidrlist){
//			SubnetUtils utils = new SubnetUtils(r);
//			//System.err.println(utils.getInfo().getNetmask());
//			System.err.println(utils.getInfo().getBroadcastAddress());
//			System.err.println(utils.getInfo().getNetworkAddress());
//			System.err.println(Arrays.asList(utils.getInfo().getAllAddresses()));
//			System.err.println();
//		}
	}
}
