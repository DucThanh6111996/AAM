package com.viettel.it.util;

import com.viettel.it.controller.ActionDbServerController;
import com.viettel.it.model.*;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.ProvinceServiceImpl;
import com.viettel.it.persistence.RelationNodeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelationNodeUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ActionDbServerController.class);
    private String messageInfo;
    
    /**
     * Lây danh sách node có liên kết đến điều kiện dừng truyền vào
     * @param srtNodeCode
     * @param conditionQeuery
     * @return
     */
    public List<RelationNode> getNodesRelateToEndOption(String srtNodeCode, String conditionQeuery, String nodeTypeCondition) {
        List<RelationNode> nodesRelationOfNode = null;
        String hql = "from RelationNode where (nodeCode = ? or nodeCodeRelation = ?)";
        try {
        	List<RelationNode> srtNodes = new RelationNodeServiceImpl().findListAll(hql, srtNodeCode, srtNodeCode);

            if (srtNodeCode != null && !srtNodes.isEmpty()) {
            	ConcurrentHashMap<String, Long> mapRelationNodePrcessed = new ConcurrentHashMap<String, Long>();
            	if (nodeTypeCondition != null) {
            		RelationNode nodeStartGetRing = null;
            		for (int i = 0; i < srtNodes.size(); i++) {
            			if (srtNodes.get(i).getNodeType() != null
            					&& srtNodes.get(i).getNodeTypeRelation() != null
            					&& !srtNodes.get(i).getNodeType().equals(nodeTypeCondition)
            					&& !srtNodes.get(i).getNodeTypeRelation().equals(nodeTypeCondition)
            					&& !srtNodes.get(i).getNodeType().equals(conditionQeuery)
            					&& !srtNodes.get(i).getNodeTypeRelation().equals(conditionQeuery)) {
            				nodeStartGetRing = srtNodes.get(i);
            			}
            			if (nodeStartGetRing != null) {
            				break;
            			}
    				}
            		
            		if (nodeStartGetRing != null) {
                		getAllNodesRelation(nodeStartGetRing, nodesRelationOfNode, conditionQeuery, nodeTypeCondition, mapRelationNodePrcessed);
                	}
            	} else {
            		RelationNode nodeStartGetRing = null;
            		for (int i = 0; i < srtNodes.size(); i++) {
            			if (srtNodes.get(i).getNodeType() != null
            					&& srtNodes.get(i).getNodeTypeRelation() != null
            					&& !srtNodes.get(i).getNodeType().equals(conditionQeuery)
            					&& !srtNodes.get(i).getNodeTypeRelation().equals(conditionQeuery)) {
            				nodeStartGetRing = srtNodes.get(i);
            			}
            			if (nodeStartGetRing != null) {
            				break;
            			}
    				}
            		
            		if (nodeStartGetRing != null) {
            			getAllNodesRelation(nodeStartGetRing, nodesRelationOfNode, conditionQeuery, nodeTypeCondition, mapRelationNodePrcessed);
            		}
            	}
            	
            }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        
        return nodesRelationOfNode;
    }

    /**
     * Lấy danh sách tất cả các node có quan hệ với Node được truyền vào đến khi
     * node type hoặc node type relation thỏa mãn condition stop
     *
     * @param link cần check
     * @param relationNodes danh sách tất cả các link kết quả
     * @param conditionStop điều kiện dùng
     */
    public void getAllNodesRelation(RelationNode link, List<RelationNode> relationNodes,
                                    String conditionStop, String nodeTypeCondition, ConcurrentHashMap<String, Long> relationNodesProcessed) {
        try {
        	if (relationNodesProcessed.get(link.getNodeCode().concat("_").concat(link.getNodeCodeRelation())) == null
        			&& relationNodesProcessed.get(link.getNodeCodeRelation().concat("_").concat(link.getNodeCode())) == null) {
        		
        		relationNodesProcessed.put(link.getNodeCode().concat("_").concat(link.getNodeCodeRelation()), link.getId());
        		relationNodesProcessed.put(link.getNodeCodeRelation().concat("_").concat(link.getNodeCode()), link.getId());
                relationNodes.add(link);

              
                // Dieu kien dung
                if (link.getNodeType() == null
                        || link.getNodeTypeRelation() == null) {
                    return;
                }
                
                if (nodeTypeCondition != null
                        && (link.getNodeType().equals(nodeTypeCondition)
                            || link.getNodeTypeRelation().equals(nodeTypeCondition))) {
                    return;
                }

                if (link.getNodeType().equals(conditionStop)
                        || link.getNodeTypeRelation().equals(conditionStop)) {
                    return;
                } else {

                    /*
                     * Lay cac node mang quan he voi node mang
                     */
                    List<RelationNode> nodesRelationOfNode;
                    String hql = "from RelationNode where id != ? "
                            + "and ((nodeCode = ? and nodeType = ?) "
                            + "or (nodeCodeRelation = ? and nodeTypeRelation = ?))";
                    nodesRelationOfNode = new RelationNodeServiceImpl().findListAll(
                            hql, link.getId(), link.getNodeCode(),
                            link.getNodeType(), link.getNodeCode(),
                            link.getNodeType());

                    List<RelationNode> nodesRelationOfNode2;
                    nodesRelationOfNode2 = new RelationNodeServiceImpl().findListAll(
                            hql, link.getId(), link.getNodeCodeRelation(),
                            link.getNodeTypeRelation(), link.getNodeCodeRelation(),
                            link.getNodeTypeRelation());

                    if (nodesRelationOfNode2 != null) {
                        nodesRelationOfNode.addAll(nodesRelationOfNode2);
                    }

                    if (nodesRelationOfNode != null) {
                        for (RelationNode nodeRelation : nodesRelationOfNode) {
                        	if (relationNodesProcessed.get(nodeRelation.getNodeCode().concat("_").concat(nodeRelation.getNodeCodeRelation())) == null
                        			&& relationNodesProcessed.get(nodeRelation.getNodeCodeRelation().concat("_").concat(nodeRelation.getNodeCode())) == null) {
                                getAllNodesRelation(nodeRelation, relationNodes, conditionStop, nodeTypeCondition, relationNodesProcessed);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Ham lay ra danh sach site router va 2 node agg
     *
     * @param lstRelationNodeSrt
     * @return
     */
    private List<Node> getRingSrt(List<RelationNode> lstRelationNodeSrt) {
        List<Node> lstRingSrt = new ArrayList<>();
        Map<String, String> mapCheckRingSrt = new HashMap<>();
        if (lstRelationNodeSrt != null && !lstRelationNodeSrt.isEmpty()) {
            Map<String, Object> filter = new HashMap<>();
            for (RelationNode relationNode : lstRelationNodeSrt) {

                if (mapCheckRingSrt.get(relationNode.getNodeCode()) == null) {
                    mapCheckRingSrt.put(relationNode.getNodeCode(), relationNode.getNodeCode());
                    try {
                        filter.put("nodeCode", relationNode.getNodeCode());
                        lstRingSrt.add(new NodeServiceImpl().findListExac(filter, null).get(0));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                
                if (mapCheckRingSrt.get(relationNode.getNodeCodeRelation()) == null) {
                    mapCheckRingSrt.put(relationNode.getNodeCodeRelation(), relationNode.getNodeCodeRelation());
                    try {
                        filter.put("nodeCode", relationNode.getNodeCodeRelation());
                        lstRingSrt.add(new NodeServiceImpl().findListExac(filter, null).get(0));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return lstRingSrt;
    }
    /**
     * Lấy Ring Agg từ danh sách Relation từ danh sách tổng thể
     * @param lstRelationNode
     * @return 
     */
    private List<Node> getRingAgg(List<RelationNode> lstRelationNode) {
        List<Node> lstRingAggNode = new ArrayList<>();
        try {
        	if (lstRelationNode != null) {       		      	
        		
                List<Node> provincesNode = getLstProvinceCoreNode(lstRelationNode);
                if (provincesNode != null && !provincesNode.isEmpty()) {

                    // duyet vong ring tu core tinh ben trai
                    lstRingAggNode.add(provincesNode.get(0));
                    RelationNodeProcessedIns.getInstance().getMapNodeProcessed().put(provincesNode.get(0).getNodeCode(), provincesNode.get(0));
                    
                    // bat dau thuc hien tim vong ring 
                    recursiveAgg(provincesNode.get(0), 
                    		lstRingAggNode, 
                    		lstRelationNode,
                    		new ConcurrentHashMap<String, String>(),
                    		new ConcurrentHashMap<Long, Long>());
                } else {
                	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
                	logger.error(">>>>>>>>>> ERROR: CANNOT GET 2 PROVICE NODE FROM AGG RING");
                }
            }
		} catch (Exception e) {
			lstRingAggNode = null;
			logger.error(e.getMessage(), e);
		}
        return lstRingAggNode;
    }

    private void recursiveAgg(Node node, List<Node> lstRingAgg,
                              List<RelationNode> lstRelationNode,
                              ConcurrentHashMap<String, String> mapNodeProcessed,
                              ConcurrentHashMap<Long, Long> mapRelationNodeProcessed) {
    	try {
    		 List<Node> relationsOfNode = getRelationsOfNode(node, lstRelationNode, null, true, mapNodeProcessed, mapRelationNodeProcessed);
    	        if (relationsOfNode != null
    	                && !relationsOfNode.isEmpty()) {
    	        	
    	        	if (relationsOfNode.size() <= 2) {
    	        		
    		            if (relationsOfNode.size() == 1) {
    		                lstRingAgg.add(relationsOfNode.get(0));
    		                mapNodeProcessed.put(relationsOfNode.get(0).getNodeCode(), relationsOfNode.get(0).getNodeCode());
    		                
    		            } else {
    		            	List<Node> lstNodeRelate;
    		            	int countRelationNodeVal = 0;
    		            	/*
    		            	 * Kiem tra xem co 2 ring trung quan he cap 1 khong
    		            	 * -> Neu ca 2 node deu co quan he voi node khac la 1 thi thong bao loi
    		            	 * -> Neu khong thi tiep tuc thuc hien tim kiem cac node AGG tiep theo
    		            	 */
    		                for (int i = 0; i < relationsOfNode.size(); i++) {
    		                	lstNodeRelate = getRelationsOfNode(relationsOfNode.get(i), lstRelationNode, relationsOfNode, false, mapNodeProcessed, mapRelationNodeProcessed);
    		                    if (lstNodeRelate != null && lstNodeRelate.size() == 1) {
    		                    	
    		                    	countRelationNodeVal++;
    		                    	if (countRelationNodeVal == 2) {
    		                    		messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.repeat");
    		                    		return;
    		                    		
    		                    	} else {
	    		                        lstRingAgg.add(relationsOfNode.get(i));
	    		                        mapNodeProcessed.put(relationsOfNode.get(i).getNodeCode(), relationsOfNode.get(i).getNodeCode());
	    		
	    		                        lstRingAgg.add(relationsOfNode.get((i == 0) ? 1 : 0));
	    		                        mapNodeProcessed.put(relationsOfNode.get((i == 0) ? 1 : 0).getNodeCode(), relationsOfNode.get((i == 0) ? 1 : 0).getNodeCode());
    		                    	}
    		                    }
    		                }
    		            }
    		            
    	        	} else {
    	        		messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.repeat");
    	        		return;
    	        	}
    	        	
    	        	/*
    	        	 * Thuc hien goi de quy de tim rin AGG tiep theo
    	        	 */
    	            recursiveAgg(lstRingAgg.get(lstRingAgg.size() - 1), lstRingAgg, lstRelationNode, mapNodeProcessed, mapRelationNodeProcessed);
    	        } else {
    	        	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
    	        }
    	        
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
       
    }

    private List<Node> getRelationsOfNode(Node node, List<RelationNode> lstRelationNode,
                                          List<Node> lstNodeCompare, boolean isAddMapProcessed,
                                          ConcurrentHashMap<String, String> mapNodeProcessed,
                                          ConcurrentHashMap<Long, Long> mapRelationNodeProcessed) {
        List<Node> relationNodesOfNode = new ArrayList<>();
        if (lstRelationNode != null) {

            Map<String, Object> filter;
            for (RelationNode o : lstRelationNode) {
                try {
                    filter = new HashMap<>();
                    if (mapRelationNodeProcessed.get(o.getId()) == null) {
                        
                        if (o.getNodeType() == null || o.getNodeTypeRelation() == null) {
                            continue;
                        }
                        
                        if (o.getNodeCode().equals(node.getNodeCode())
                                && (o.getNodeTypeRelation().equals(Config.AGG_NODE_TYPE)
                                || o.getNodeTypeRelation().equals(Config.PROVINCE_CORE_NODE_TYPE))) {
                            filter.put("nodeCode", o.getNodeCodeRelation());

                        } else if (o.getNodeCodeRelation().equals(node.getNodeCode())
                                && (o.getNodeType().equals(Config.AGG_NODE_TYPE)
                                || o.getNodeType().equals(Config.PROVINCE_CORE_NODE_TYPE))) {
                            filter.put("nodeCode", o.getNodeCode());
                        }

                        if (!filter.isEmpty()) {
                            Node nodeRelation = new NodeServiceImpl().findListExac(filter, null).get(0);
                            if (mapNodeProcessed.get(nodeRelation.getNodeCode()) == null) {
                                relationNodesOfNode.add(nodeRelation);
                            }

                            if (isAddMapProcessed) {
                                if (lstNodeCompare != null && !lstNodeCompare.isEmpty()) {
                                    boolean isExist = false;
                                    for (Node n : lstNodeCompare) {
                                        if (n.getNodeCode().equals(nodeRelation.getNodeCode())) {
                                            isExist = true;
                                        }
                                    }
                                    if (!isExist) {
                                    	mapRelationNodeProcessed.put(o.getId(), o.getId());
                                    }
                                } else {
                                    mapRelationNodeProcessed.put(o.getId(), o.getId());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } // end loop for
        }
        return relationNodesOfNode;
    }

    /**
     * Ham lay ra 2 core tinh tu danh sach relation node
     * @param relationsNode: Danh sach quan he cac node cua ring agg
     * @return List node mang chua 2 core tinh
     */
    private List<Node> getLstProvinceCoreNode(List<RelationNode> relationsNode) {
        List<Node> provincesNodeOrders = new ArrayList<>();
        try {
            if (relationsNode != null) {
                List<RelationNode> provincesCoreNode = new ArrayList<>();
                for (RelationNode node : relationsNode) {
                    if (node.getNodeType() == null || node.getNodeTypeRelation() == null) {
                        continue;
                    }
                    
                    if (node.getNodeType().equals(Config.PROVINCE_CORE_NODE_TYPE)
                            || node.getNodeTypeRelation().equals(Config.PROVINCE_CORE_NODE_TYPE)) {
                        boolean isExistNode = false;
                        for (RelationNode check : provincesCoreNode) {
                            if (check.getNodeCode().equals(node.getNodeCode())
                                    || check.getNodeCode().equals(node.getNodeCodeRelation())
                                    || check.getNodeCodeRelation().equals(node.getNodeCode())
                                    || check.getNodeCodeRelation().equals(node.getNodeCodeRelation())) {
                                isExistNode = true;
                                break;
                            }
                        }
                        if (!isExistNode) {
                            provincesCoreNode.add(node);
                        }
                    }
                }

                if (provincesCoreNode != null && provincesCoreNode.size() >= 2) {
                    // lay ra thong tin core tinh thu 1
                    String provinceNodeCode1 = (provincesCoreNode.get(0).getNodeType().equals(Config.PROVINCE_CORE_NODE_TYPE)
                            ? provincesCoreNode.get(0).getNodeCode() : provincesCoreNode.get(0).getNodeCodeRelation());
                    // lay ra thong tin core tinh thu 2
                    String provinceNodeCode2 = (provincesCoreNode.get(1).getNodeType().equals(Config.PROVINCE_CORE_NODE_TYPE)
                            ? provincesCoreNode.get(1).getNodeCode() : provincesCoreNode.get(1).getNodeCodeRelation());

                    Map<String, Object> filter = new HashMap<>();
                    filter.put("nodeCode", provinceNodeCode1);
                    Node provinceNode1 = (new NodeServiceImpl().findListExac(filter, null)).get(0);

                    filter.put("nodeCode", provinceNodeCode2);
                    Node provinceNode2 = (new NodeServiceImpl().findListExac(filter, null)).get(0);

                    if (ipToLong(provinceNode1.getNodeIp()) < ipToLong(provinceNode2.getNodeIp())) {
                        provincesNodeOrders.add(provinceNode1);
                        provincesNodeOrders.add(provinceNode2);
                    } else {
                        provincesNodeOrders.add(provinceNode2);
                        provincesNodeOrders.add(provinceNode1);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return provincesNodeOrders;
    }

    public long ipToLong(String ipAddress) {

        String[] ipAddressInArray = ipAddress.split("\\.");

        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }

        return result;
    }

    /**
     * Ham lay ra danh sach cac SRT va AGG can khai bao dich vu
     *
     * @param srtNodecode (Ma SRT hoac SWICTH khach hang dau truc tiep den)
     * @return
     */
    public List<Node> getRingSrt(String srtNodecode) {
    	messageInfo = null;
        List<Node> lstRingSrtNode = new ArrayList<>();
        try {
            Node clientNodeConnect;
            Map<String, Object> filter = new HashMap<>();
            filter.put("nodeCode", srtNodecode);
            List<Node> lstNode = new NodeServiceImpl().findListExac(filter, null);
            if (lstNode != null && !lstNode.isEmpty()) {
                clientNodeConnect = lstNode.get(0);
            } else {
                return null;
            }
            
            // Lay ra ring SRT relation
            List<RelationNode> srtRelationNodes = getNodesRelateToEndOption(srtNodecode, Config.AGG_NODE_TYPE, null);
            if (srtRelationNodes == null || srtRelationNodes.isEmpty()) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.srt.open");
            	return null;
            }
            
            // Lay ra ring SRT Node mang
            lstRingSrtNode = getRingSrt(srtRelationNodes);
            if (lstRingSrtNode == null || lstRingSrtNode.isEmpty()) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.srt.open");
            	return null;
            }
            
            logger.info("ring srt===============");
            for(Node srt : lstRingSrtNode) {
            	logger.info(srt.getNodeCode());
            }
            logger.info("end ring srt===============");
            
            // Lay ra ma AGG de lay vong ring AAG den core tinh
            String aggNodeCode = null;
            for (Node srtRing : lstRingSrtNode) {
            	if (srtRing.getNodeType().getTypeName().equals(Config.AGG_NODE_TYPE)) {
            		aggNodeCode = srtRing.getNodeCode();
            	}
            }
            
            // Neu khong lay duoc AGG node code
            if (aggNodeCode == null) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.srt.open");
            	logger.error(">>>>>>>>>> ERROR: AGG NODE NOT FOUND IN SRT RING");
            	return null;
            }
            
            
            // Lay ra vong ring agg noi den core tinh
            List<RelationNode> allRelationNodes = getNodesRelateToEndOption(aggNodeCode, Config.PROVINCE_CORE_NODE_TYPE, Config.SRT_NODE_TYPE);
            
            if (allRelationNodes == null || allRelationNodes.isEmpty()) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
            	logger.error(">>>>>>>>>> CANNOT GET RING AGG RELATION");
            	return null;
            }
            
            // Lay ra vong ring agg node mang
            List<Node> lstRingAggNode = getRingAgg(allRelationNodes);
            
            if (lstRingAggNode == null || lstRingAggNode.isEmpty()) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
            	logger.error(">>>>>>>>>>> CANNOT GET AGG RING NODE");
            	return null;
            }
            
            logger.info("=========== ring agg ===============");
            for (Node agg : lstRingAggNode) {
            	logger.info(agg.getNodeCode());
            }
            logger.info("=========== end ring agg ===============");
            // Lay ra 2 AGG
            Node aggNode1 = null;
            Node aggNode2 = null;
            int indxAgg1InSrtRing = -1;
            int indxAgg2InSrtRing = -1;
            int indx = 0;
            for (Node strRing : lstRingSrtNode) {
                if (strRing.getNodeType().getTypeName().equals(Config.AGG_NODE_TYPE)) {
                    if (aggNode1 == null) {
                        aggNode1 = strRing;
                        indxAgg1InSrtRing = indx;
                    } else if (aggNode2 == null) {
                        aggNode2 = strRing;
                        indxAgg2InSrtRing = indx;
                    } else if (aggNode1 != null && aggNode2 != null) {
                        break;
                    }
                }
                indx++;
            }

            /*
             * Loi vong ring bi ho
             */
            if (aggNode1 == null || aggNode2 == null) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
                return null;
            }

            /*
             * Xac dinh node AGG trai hay phai
             */
            int indexOfAggNode1 = -1;
            int indexOfAggNode2 = -1;
            for (int i = 0; i < lstRingAggNode.size(); i++) {
                if (aggNode1.getNodeCode().equals(lstRingAggNode.get(i).getNodeCode())) {
                    indexOfAggNode1 = i;
                }
                if (aggNode2.getNodeCode().equals(lstRingAggNode.get(i).getNodeCode())) {
                    indexOfAggNode2 = i;
                }
                if (indexOfAggNode1 != -1 && indexOfAggNode2 != -1) {
                    break;
                }
            }
            
            if (indexOfAggNode1  == -1 && indexOfAggNode2 == -1) {
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.open");
                return null;
            }

            if (Math.abs(indexOfAggNode1 - indexOfAggNode2) != 1) {
            	// neu 2 node mang agg khong lien ke nhau thi thong bao loi
            	messageInfo = MessageUtil.getResourceBundleMessage("label.err.ring.agg.adjacent");
                return null;
                
            } else {
            	
                // Gan danh sach interface down cho 2 node AGG
                lstRingSrtNode.get(indxAgg1InSrtRing).setLstInterface(getInterfacesOfNode(lstRingSrtNode.get(indxAgg1InSrtRing), Config.SRT_NODE_TYPE, Config.SRT_NODE_TYPE));
                lstRingSrtNode.get(indxAgg2InSrtRing).setLstInterface(getInterfacesOfNode(lstRingSrtNode.get(indxAgg2InSrtRing), Config.SRT_NODE_TYPE, Config.SRT_NODE_TYPE));
                if (indexOfAggNode1 % 2 == 0) {
                    lstRingSrtNode.get(indxAgg1InSrtRing).setFlag(Config.AGG_NODE_EVEN);
                    lstRingSrtNode.get(indxAgg2InSrtRing).setFlag(Config.AGG_NODE_ODD);
                } else {
                    lstRingSrtNode.get(indxAgg1InSrtRing).setFlag(Config.AGG_NODE_ODD);
                    lstRingSrtNode.get(indxAgg2InSrtRing).setFlag(Config.AGG_NODE_EVEN);
                }
            }

            /*
             * Tim ra SRT chinh de thuc hien khai bao
             */
            if (clientNodeConnect.getNodeType().getTypeName().equals(Config.SWITCH_NODE_TYPE)) {
                List<RelationNode> switchRelationsNode = getNodesRelateToEndOption(clientNodeConnect.getNodeCode(), Config.SRT_NODE_TYPE, null);
                if (switchRelationsNode != null && !switchRelationsNode.isEmpty()) {
                    String srtNodeCode = null;
                    for (RelationNode sw : switchRelationsNode) {
                        if (sw.getNodeType().trim().equals(Config.SRT_NODE_TYPE)) {
                            srtNodeCode = sw.getNodeCode();
                        } else if (sw.getNodeTypeRelation().trim().equals(Config.SRT_NODE_TYPE)) {
                            srtNodeCode = sw.getNodeCodeRelation();
                        }
                    }

                    if (srtNodeCode != null) {
                        for (int i = 0; i < lstRingSrtNode.size(); i++) {
                            if (lstRingSrtNode.get(i).getNodeCode().trim().equals(srtNodecode)) {
                                lstRingSrtNode.get(i).setFlag(Config.SRT_NODE_CLIENT_CONNECT_DIRECTOR);
                                break;
                            }
                        }
                    } else {
                        return null;
                    }

                } else {
                    return null;
                }

            } else if (clientNodeConnect.getNodeType().getTypeName().equals(Config.SRT_NODE_TYPE)) {
                for (int i = 0; i < lstRingSrtNode.size(); i++) {
                    if (lstRingSrtNode.get(i).getNodeCode().trim().equals(clientNodeConnect.getNodeCode().trim())) {
                        lstRingSrtNode.get(i).setFlag(Config.SRT_NODE_CLIENT_CONNECT_DIRECTOR);
                        break;
                    }
                }
            }

            /*
             * Gan gia tri cac Interface cho cac node SRT cua vong ring
             */
            for (int i = 0; i < lstRingSrtNode.size(); i++) {
                if (!lstRingSrtNode.get(i).getNodeType().getTypeName().trim().equals(Config.AGG_NODE_TYPE)) {
                    lstRingSrtNode.get(i).setLstInterface(getInterfacesOfNode(lstRingSrtNode.get(i), Config.SRT_NODE_TYPE, Config.AGG_NODE_TYPE));
                }
            }

            /*
             * Gan lai message thong bao loi neu danh sach ring SRT node trong
             */
            if (lstRingSrtNode != null && !lstRingSrtNode.isEmpty()) {
            	messageInfo = null;
            }
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstRingSrtNode;
    }

    private List<String> getInterfacesOfNode(Node node, String nodeTypeCondition1, String nodeTypeCondition2) {
        List<String> lstInterface = new ArrayList<>();
        try {
            List<RelationNode> nodesRelationOfNode;
            String hql = "from RelationNode where (nodeCode = ? or nodeCodeRelation = ?)";
            nodesRelationOfNode = new RelationNodeServiceImpl().findList(
                    hql, 1, 1000, node.getNodeCode(), node.getNodeCode());

            if (nodesRelationOfNode != null && !nodesRelationOfNode.isEmpty()) {
                for (RelationNode o : nodesRelationOfNode) {

                    if (o.getNodeType() == null || o.getNodeTypeRelation() == null) {
                        continue;
                    }
                    
                    if (o.getNodeCode().equals(node.getNodeCode())
                            && (o.getNodeTypeRelation().equals(nodeTypeCondition1.trim())
                            || o.getNodeTypeRelation().equals(nodeTypeCondition2.trim()))) {
                        lstInterface.add(o.getInterfacePortRelation());

                    } else if (o.getNodeCodeRelation().equals(node.getNodeCode())
                            && (o.getNodeType().equals(nodeTypeCondition1.trim())
                            || o.getNodeType().equals(nodeTypeCondition2.trim()))) {
                        lstInterface.add(o.getInterfacePort());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstInterface;
    }


    /**
     * @param lstRingSrtNode
     * @return
     * @author huynx6
     * 
     */
    public int getRingType(List<Node> lstRingSrtNode){
    	 // Get AGG index
         int indxAgg1 = -1;
         int indxAgg2 = -1;
         for (int i = 0; i < lstRingSrtNode.size(); i++) {
             if (lstRingSrtNode.get(i).getFlag()!=null && (lstRingSrtNode.get(i).getFlag().equals(Config.AGG_NODE_EVEN)
                     || lstRingSrtNode.get(i).getFlag().equals(Config.AGG_NODE_ODD))) {
                 if (indxAgg1 == -1) {
                     indxAgg1 = i;
                 } else if (indxAgg2 == -1) {
                     indxAgg2 = i;
                 } else {
                     break;
                 }
             }
         }
         int ringType = 0;

         if (indxAgg1 != -1 && indxAgg2 != -1) {
			ringType = ParamUtil.TYPE_NORMAL;
             if (lstRingSrtNode.get(indxAgg1).getFlag().equals(Config.AGG_NODE_EVEN)) {
                 ringType = ParamUtil.TYPE_CASCADE;
             }
         }
         return ringType;
    }


    private Node getNodeFromRingSrt(List<Node> lstRingSrtNode, String flag) {
        for (Node node : lstRingSrtNode) {
            if (node.getFlag().equals(flag)) {
                return node;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {

            RelationNodeUtil relation = new RelationNodeUtil();
            long start = System.currentTimeMillis();
//            List<Node> lstRingSrt = relation.getRingSrt("HNI8937SRT01");
           
//            List<Node> lstRingSrt = relation.getRingSrt("BGG0284SRT01");
//            List<Node> lstRingSrt = relation.getRingSrt("HNI8937SRT01");
            
            List<Node> lstRingSrt = relation.getRingSrt("BGG0289SRT01");
            System.out.println("RESSUUUUUUUUUT: " + (System.currentTimeMillis() - start));
            for (Node n : lstRingSrt) {
                System.out.println(n.getNodeCode() + "-" + n.getNodeIp() + "-" + n.getNodeName() + "-" + n.getFlag());
            }
//			List<RelationNode> allRelationNodes = relation.getNodesRelateOfSTR("BGG0284SRT01", Config.PROVINCE_CORE_NODE_TYPE);
//			List<RelationNode> aggRelationNodes = relation.getNodesRelateOfSTR("BGG0284SRT01",Config.AGG_NODE_TYPE);
//			
//			List<Node> ringAggsNode = relation.getRingAgg(allRelationNodes);
//			List<Node> ringSrtNode = relation.getRingSrt(aggRelationNodes);
//			
//			for (Node agg : ringAggsNode) {
//				System.out.println(agg.getNodeCode());
//			}
//			
//			// Lay ra 2 site router
//			Node aggNode1 = null;
//			Node aggNode2 = null;
//			int indxAgg1InSrtRing = -1;
//			int indxAgg2InSrtRing = -1;
//			int indx = 0;
//			for (Node strRing : ringSrtNode) {
//				if (strRing.getNodeType().getTypeName().equals(Config.AGG_NODE_TYPE)) {
//					if (aggNode1 == null) {
//						aggNode1 = strRing;
//						indxAgg1InSrtRing = indx;
//					} else if (aggNode2 == null) {
//						aggNode2 = strRing;
//						indxAgg2InSrtRing = indx;
//					} else if (aggNode1 != null && aggNode2 != null) {
//						break;
//					}
//				}
//				indx++;
//			}
//			
//			/**
//			 * Xac dinh node AGG trai hay phai
//			 */
//			int indexOfAggNode1 = -1;
//			int indexOfAggNode2 = -1;
//			for (int i = 0; i < ringAggsNode.size(); i++) {
//				if (aggNode1.getNodeCode().equals(ringAggsNode.get(i).getNodeCode())) {
//					indexOfAggNode1 = i;
//				}
//				if (aggNode2.getNodeCode().equals(ringAggsNode.get(i).getNodeCode())) {
//					indexOfAggNode2 = i;
//				}
//				if (indexOfAggNode1 != -1 && indexOfAggNode2 != -1) {
//					break;
//				}
//			}
//			
//			if (Math.abs(indexOfAggNode1 - indexOfAggNode2) != 1) {
//				System.out.println("false");
//			} else {
//				
//				if(indexOfAggNode1 % 2 == 0) {
//					ringSrtNode.get(indxAgg1InSrtRing).setFlag(Config.AGG_NODE_ODD);
//					ringSrtNode.get(indxAgg2InSrtRing).setFlag(Config.AGG_NODE_EVEN);
//				} else {
//					ringSrtNode.get(indxAgg1InSrtRing).setFlag(Config.AGG_NODE_EVEN);
//					ringSrtNode.get(indxAgg2InSrtRing).setFlag(Config.AGG_NODE_ODD);
//				}
//			}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

	public String getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(String messageInfo) {
		this.messageInfo = messageInfo;
	}
}
