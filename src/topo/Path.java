package topo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Switch.Space;
import Switch.Switch;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.SetOper;

public class Path {
	private String pathName;
	public Path(String pathName) {
		this.setPathName(pathName);
	}
	
	
	
	public void mapNoRuleAddFlows(JSONArray path, ArrayList<String> securitySpaces
			, HashMap<String, Switch> switchs) {
		List<Space> spaces = new ArrayList<Space>();
		List<ArrayList<Space>> samePortSpaces = new ArrayList<ArrayList<Space>>();
		System.out.println("add flows===========path is below:");
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);

			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			ArrayList<Space> samePorts = switchs.get(switchName).samePortSpace(portStart);
			samePortSpaces.add(samePorts);
			spaces.add(space);
		}
		//#for(String securitySpace: securitySpaces) {
			System.out.println("add flows function : securitySpaces size is " + securitySpaces);
			ArrayList<String> newSecurity = new ArrayList<String>();
			newSecurity.addAll(securitySpaces);
			//经过每一个交换机的space空间后，securitySpace都会发生变化
			int spaceCount = 0;
			for(Space space: spaces) {
				//#System.out.println("in Path computeFlowsModified");
				//#System.out.println("securitySpace is " + securitySpace);
				ArrayList<Space> samePortSpace = samePortSpaces.get(spaceCount);
				spaceCount++;
				
				ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
				for(String inputSpace: newInputSpace) {
					SetOper.addNoRepeat(newSecurity, inputSpace);
				}
				
				ArrayList<String> nextInput = new ArrayList<String>();
				for(String strOne: newSecurity) {
					nextInput.add(strOne);
				}
				
				if(newSecurity.size()!=0) {
					System.out.println("需要添加允许流表");
					System.out.println(newSecurity);
					//希望addAllowedSpace能够说明具体加入了几项流表
					int addFlows = space.addAllowedSpaces(newSecurity);
					
					//同时同一主机相同入端口的交换机的其他Space需要更新wrongPortSpace
					for(Space sameSpace: samePortSpace) {
						sameSpace.refreshWrongPortSpace(newSecurity);
					}
				}
				newSecurity = nextInput;
			}
	}
	
	
	//除了需要添加flows之外，还完成了端口带宽的缩减
	public void addFlows(JSONArray path, ArrayList<String> securitySpaces
			, HashMap<String, Switch> switchs, Double band) {
		List<Space> spaces = new ArrayList<Space>();
		List<ArrayList<Space>> samePortSpaces = new ArrayList<ArrayList<Space>>();
		System.out.println("add flows===========path is below:");
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);

			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			System.out.println("映射前， switch：" + switchName + "端口 : " + portStart + "带宽总共是" 
					+ switchs.get(switchName).getPortLink().get(portStart).getBandTotal());
			
			System.out.println("映射前， switch：" + switchName + "端口 : " + portEnd + "带宽总共是" 
					+ switchs.get(switchName).getPortLink().get(portEnd).getBandTotal());

			System.out.println("映射前， switch：" + switchName + "端口 : " + portStart + "带宽利用了" 
					+ switchs.get(switchName).getPortLink().get(portStart).getBandUsed());
			
			System.out.println("映射前， switch：" + switchName + "端口 : " + portEnd + "带宽利用了" 
					+ switchs.get(switchName).getPortLink().get(portEnd).getBandUsed());
			
			switchs.get(switchName).getPortLink().get(portStart).consumeBand(band);
			switchs.get(switchName).getPortLink().get(portEnd).consumeBand(band);		
			
			System.out.println("映射后， switch：" + switchName + "端口 : " + portStart + "带宽利用了" 
					+ switchs.get(switchName).getPortLink().get(portStart).getBandUsed());
			
			System.out.println("映射后， switch：" + switchName + "端口 : " + portEnd + "带宽利用了" 
					+ switchs.get(switchName).getPortLink().get(portEnd).getBandUsed());
			
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			ArrayList<Space> samePorts = switchs.get(switchName).samePortSpace(portStart);
			samePortSpaces.add(samePorts);
			spaces.add(space);
		}
		//#for(String securitySpace: securitySpaces) {
			System.out.println("add flows function : securitySpaces size is " + securitySpaces);
			ArrayList<String> newSecurity = new ArrayList<String>();
			newSecurity.addAll(securitySpaces);
			//经过每一个交换机的space空间后，securitySpace都会发生变化
			int spaceCount = 0;
			for(Space space: spaces) {
				//#System.out.println("in Path computeFlowsModified");
				//#System.out.println("securitySpace is " + securitySpace);
				ArrayList<Space> samePortSpace = samePortSpaces.get(spaceCount);
				spaceCount++;
				ArrayList<String> allowedSpace = space.getAllowedSpace();
				ArrayList<String> deniedSpace = space.getDeniedSpace();
				ArrayList<String> highPrioSpace = space.getHigh();
				
				//既然选择添加这些表项，那么这些表项默认不会与wrongPortSpace产生交集
				//#ArrayList<String> wrongPortSpace = space.getWrongPortSpace();
				//#System.out.println("allowedSpace is " + allowedSpace);
				//#System.out.println("deniedSpace is " + deniedSpace);
				//先判断与NAT空间的交集，生成新的输入空间
				ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
				//再判断与wrongPortSpace的交集，一旦有交集，这个路径就失效了

				//新的输入空间如果满足包含于旧的输入空间，则任何表项都不需要
				//与denied space的相交的空间，然后相交的空间与高优先级的允许空间对比，然后再添加表项
				//#System.out.println("newInputSpace is " + newInputSpace);
				//这个newInputSpace可能与就的输入空间securitySpace有交集,所以我们
				//计算出他们的交集，然后将securitySpace中去除掉交集的部分重新添加到newInputSpace上
				//SetOper.addNoRepeat(newInputSpace, newSecurity);			
				for(String inputSpace: newInputSpace) {
					//System.out.println("inputSpace is" + inputSpace);
					SetOper.addNoRepeat(newSecurity, inputSpace);
					//System.out.println("finish newSecurity is " + newSecurity);
				}
				
				ArrayList<String> nextInput = new ArrayList<String>();
				for(String strOne: newSecurity) {
					nextInput.add(strOne);
				}
				
				
				ArrayList<String> interSectWithDenied = SetOper.interSect(newSecurity, deniedSpace);
				
				//判断与deniedSpace的交集是否在priority space里面，如果有交集，则计算出去除交集，也就是需要添加的空间
				ArrayList<String> spaceWithOutPrior = SetOper.noInterSect(highPrioSpace, interSectWithDenied);
				
				if(spaceWithOutPrior.size()!=0) {
					//System.out.println("!!!!已经添加高优先级流表!!!!!");
					//System.out.println("高优先级流表为");
					//System.out.println(spaceWithOutPrior);
					space.addHighPriority(spaceWithOutPrior);
					newSecurity = SetOper.removeInterFromList(newSecurity, interSectWithDenied);
					for(Space sameSpace: samePortSpace) {
						sameSpace.refreshWrongPortSpace(spaceWithOutPrior);
						//System.out.println("更新高优先级空间后，samePort的wrongPort空间分别为");
						//System.out.println(sameSpace.getWrongPortSpace());
					}
				}
				
				if(newSecurity.size()!=0) {
					System.out.println("需要添加允许流表");
					System.out.println(newSecurity);
					//希望addAllowedSpace能够说明具体加入了几项流表
					int addFlows = space.addAllowedSpaces(newSecurity);
					
					//同时同一主机相同入端口的交换机的其他Space需要更新wrongPortSpace
					for(Space sameSpace: samePortSpace) {
						sameSpace.refreshWrongPortSpace(newSecurity);
						//System.out.println("更新允许空间后，samePort的wrongPort空间分别为");
						//System.out.println(sameSpace.getWrongPortSpace());
					}
				}
				
				newSecurity = nextInput;
				
				//否则集合之外的还要添加一条highPriority的表项
/*				ArrayList<String> deniedResult = Util.computeIntersect(deniedSpace, securitySpace);
				if(deniedResult.size()==0) {
					System.out.println("no intersect");
				}else {
					flowNum++;
					//添加流表项，同时更新新的allowedSpace
					allowedSpace = Util.union(allowedSpace, deniedResult);
				}
				
				System.out.println("after conflict discover, allowedSpace is" + allowedSpace);
				System.err.println("flowNumber is " + flowNum);
				
				if(natSpace.equals("")) {
					continue;
				}
				String natResult = Util.computeIntersect(securitySpace, natSpace);
				if(natResult.contains("z")) {
					System.out.println("no intersect");
				}else {
					String newInputSpace = space.copyNatOperations(natResult);
					String interSect = Util.computeIntersect(newInputSpace, allowedSpace);
					if(interSect.contains("z")|| !interSect.equals(newInputSpace)) {
						//添加新的流表
						flowNum++;
						allowedSpace = Util.union(allowedSpace, newInputSpace);
					}
					//更新新的securitySpace
					securitySpace = Util.union(securitySpace, newInputSpace);
				}*/
			}
		//#}
			//System.out.println("==============success==============");
	}
	
	public boolean mapNoRuleCheckAvail(JSONArray path, ArrayList<String> securitySpaces,
			HashMap<String, Switch> switchs) {
		List<Space> spaces = new ArrayList<Space>();
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);
			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			spaces.add(space);
		}
		ArrayList<String> newSecurity = new ArrayList<String>();
		newSecurity.addAll(securitySpaces);
		//经过每一个交换机的space空间后，securitySpace都会发生变化
		for(Space space: spaces) {
			//#System.out.println("in Path computeFlowsModified");
			//#System.out.println("securitySpace is " + securitySpace);
			ArrayList<String> allowedSpace = space.getAllowedSpace();
			ArrayList<String> deniedSpace = space.getDeniedSpace();
			ArrayList<String> highPrioSpace = space.getHigh();
			ArrayList<String> wrongPortSpace = space.getWrongPortSpace();
			//#System.out.println("allowedSpace is " + allowedSpace);
			//#System.out.println("deniedSpace is " + deniedSpace);
			//先判断与NAT空间的交集，生成新的输入空间
			ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
			//再判断与wrongPortSpace的交集，一旦有交集，这个路径就失效了

			//新的输入空间如果满足包含于旧的输入空间，则任何表项都不需要
			//与denied space的相交的空间，然后相交的空间与高优先级的允许空间对比，然后再添加表项
			//#System.out.println("newInputSpace is " + newInputSpace);
			//这个newInputSpace可能与就的输入空间securitySpace有交集,所以我们
			//计算出他们的交集，然后将securitySpace中去除掉交集的部分重新添加到newInputSpace上
			//SetOper.addNoRepeat(newInputSpace, newSecurity);			
			for(String inputSpace: newInputSpace) {
				//System.out.println("inputSpace is" + inputSpace);
				SetOper.addNoRepeat(newSecurity, inputSpace);
				//System.out.println("finish newSecurity is " + newSecurity);
			}
			
			ArrayList<String> nextInput = new ArrayList<String>();
			for(String strOne: newSecurity) {
				nextInput.add(strOne);
			}
			//最后产生的newInputSpace才是下一个Space真正的输入
			if(SetOper.interSectWithWrongPort(wrongPortSpace, newSecurity)) {
				return false;
			}
			//计算与denied空间的交集，如果交集全部都在highPriority空间中，那么只需要加上输入空间这条表项
			//可以证明这个交集的空间，内部一定是互不相交的
			ArrayList<String> interSectWithDenied = SetOper.interSect(newSecurity, deniedSpace);
			if(interSectWithDenied.size()!=0) {
				return false;
			}
			newSecurity = nextInput;
		}
		return true;
	}
	
	public boolean ruleNoMapCheckAvail(JSONArray path, ArrayList<String> securitySpaces,
			HashMap<String, Switch> switchs) {
		List<Space> spaces = new ArrayList<Space>();
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);
			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			spaces.add(space);
		}
		ArrayList<String> newSecurity = new ArrayList<String>();
		newSecurity.addAll(securitySpaces);
		//经过每一个交换机的space空间后，securitySpace都会发生变化
		for(Space space: spaces) {
			//#System.out.println("in Path computeFlowsModified");
			//#System.out.println("securitySpace is " + securitySpace);
			ArrayList<String> allowedSpace = space.getAllowedSpace();
			ArrayList<String> deniedSpace = space.getDeniedSpace();
			ArrayList<String> highPrioSpace = space.getHigh();
			ArrayList<String> wrongPortSpace = space.getWrongPortSpace();
			//#System.out.println("allowedSpace is " + allowedSpace);
			//#System.out.println("deniedSpace is " + deniedSpace);
			//先判断与NAT空间的交集，生成新的输入空间
			ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
			//再判断与wrongPortSpace的交集，一旦有交集，这个路径就失效了

			//新的输入空间如果满足包含于旧的输入空间，则任何表项都不需要
			//与denied space的相交的空间，然后相交的空间与高优先级的允许空间对比，然后再添加表项
			//#System.out.println("newInputSpace is " + newInputSpace);
			//这个newInputSpace可能与就的输入空间securitySpace有交集,所以我们
			//计算出他们的交集，然后将securitySpace中去除掉交集的部分重新添加到newInputSpace上
			//SetOper.addNoRepeat(newInputSpace, newSecurity);			
			for(String inputSpace: newInputSpace) {
				//System.out.println("inputSpace is" + inputSpace);
				SetOper.addNoRepeat(newSecurity, inputSpace);
				//System.out.println("finish newSecurity is " + newSecurity);
			}
			
			ArrayList<String> nextInput = new ArrayList<String>();
			for(String strOne: newSecurity) {
				nextInput.add(strOne);
			}
			//最后产生的newInputSpace才是下一个Space真正的输入
			if(SetOper.interSectWithWrongPort(wrongPortSpace, newSecurity)) {
				return false;
			}

			newSecurity = nextInput;
		}
		return true;
	}
	
	public void ruleNoMapAddFlows(JSONArray path, ArrayList<String> securitySpaces,
			HashMap<String, Switch> switchs) {
		List<Space> spaces = new ArrayList<Space>();
		List<ArrayList<Space>> samePortSpaces = new ArrayList<ArrayList<Space>>();
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);
			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			ArrayList<Space> samePorts = switchs.get(switchName).samePortSpace(portStart);
			samePortSpaces.add(samePorts);
			spaces.add(space);
		}
		ArrayList<String> newSecurity = new ArrayList<String>();
		newSecurity.addAll(securitySpaces);
		//经过每一个交换机的space空间后，securitySpace都会发生变化
		int spaceCount = 0;
		for(Space space: spaces) {
			//#System.out.println("in Path computeFlowsModified");
			//#System.out.println("securitySpace is " + securitySpace);
			ArrayList<Space> samePortSpace = samePortSpaces.get(spaceCount);
			spaceCount++;
			ArrayList<String> allowedSpace = space.getAllowedSpace();
			ArrayList<String> deniedSpace = space.getDeniedSpace();
			ArrayList<String> highPrioSpace = space.getHigh();
			
			//既然选择添加这些表项，那么这些表项默认不会与wrongPortSpace产生交集
			//#ArrayList<String> wrongPortSpace = space.getWrongPortSpace();
			//#System.out.println("allowedSpace is " + allowedSpace);
			//#System.out.println("deniedSpace is " + deniedSpace);
			//先判断与NAT空间的交集，生成新的输入空间
			ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
			//再判断与wrongPortSpace的交集，一旦有交集，这个路径就失效了

			//新的输入空间如果满足包含于旧的输入空间，则任何表项都不需要
			//与denied space的相交的空间，然后相交的空间与高优先级的允许空间对比，然后再添加表项
			//#System.out.println("newInputSpace is " + newInputSpace);
			//这个newInputSpace可能与就的输入空间securitySpace有交集,所以我们
			//计算出他们的交集，然后将securitySpace中去除掉交集的部分重新添加到newInputSpace上
			//SetOper.addNoRepeat(newInputSpace, newSecurity);			
			for(String inputSpace: newInputSpace) {
				//System.out.println("inputSpace is" + inputSpace);
				SetOper.addNoRepeat(newSecurity, inputSpace);
				//System.out.println("finish newSecurity is " + newSecurity);
			}
			
			ArrayList<String> nextInput = new ArrayList<String>();
			for(String strOne: newSecurity) {
				nextInput.add(strOne);
			}
			
			
			ArrayList<String> interSectWithDenied = SetOper.interSect(newSecurity, deniedSpace);
			
			//判断与deniedSpace的交集是否在priority space里面，如果有交集，则计算出去除交集，也就是需要添加的空间
			ArrayList<String> spaceWithOutPrior = SetOper.noInterSect(highPrioSpace, interSectWithDenied);
			
			if(spaceWithOutPrior.size()!=0) {
				//System.out.println("!!!!已经添加高优先级流表!!!!!");
				//System.out.println("高优先级流表为");
				//System.out.println(spaceWithOutPrior);
				space.addHighPriority(spaceWithOutPrior);
				newSecurity = SetOper.removeInterFromList(newSecurity, interSectWithDenied);
				for(Space sameSpace: samePortSpace) {
					sameSpace.refreshWrongPortSpace(spaceWithOutPrior);
					//System.out.println("更新高优先级空间后，samePort的wrongPort空间分别为");
					//System.out.println(sameSpace.getWrongPortSpace());
				}
			}
			
			if(newSecurity.size()!=0) {
				System.out.println("需要添加允许流表");
				System.out.println(newSecurity);
				//希望addAllowedSpace能够说明具体加入了几项流表
				int addFlows = space.addAllowedSpaces(newSecurity);
				
				//同时同一主机相同入端口的交换机的其他Space需要更新wrongPortSpace
				for(Space sameSpace: samePortSpace) {
					sameSpace.refreshWrongPortSpace(newSecurity);
					//System.out.println("更新允许空间后，samePort的wrongPort空间分别为");
					//System.out.println(sameSpace.getWrongPortSpace());
				}
			}
			newSecurity = nextInput;
		}
	}
	
	public int computeFlowsModified(JSONArray path, ArrayList<String> securitySpaces,
			HashMap<String, Switch> switchs) {
		List<Space> spaces = new ArrayList<Space>();
		System.out.println("===========path is below:");
		for(Object switchObject : path) {
			JSONObject switchJSON = JSONObject.fromObject(switchObject);
			String switchName = switchJSON.getString("switch");
			System.out.println("switch is " + switchName);
			String portStart = switchJSON.getString("start");
			System.out.println("start port is " + portStart);
			String portEnd = switchJSON.getString("end");
			System.out.println("end port is " + portEnd);
			Space space = switchs.get(switchName).getSpace(portStart, portEnd);
			spaces.add(space);
		}
		//#int totalFlows = 0;
		//#for(String securitySpace: securitySpaces) {
		//#	System.out.println("securitySpaces size is " + securitySpace);
			int flowNum = 0;
			ArrayList<String> newSecurity = new ArrayList<String>();
			newSecurity.addAll(securitySpaces);
			//经过每一个交换机的space空间后，securitySpace都会发生变化
			for(Space space: spaces) {
				//#System.out.println("in Path computeFlowsModified");
				//#System.out.println("securitySpace is " + securitySpace);
				ArrayList<String> allowedSpace = space.getAllowedSpace();
				ArrayList<String> deniedSpace = space.getDeniedSpace();
				ArrayList<String> highPrioSpace = space.getHigh();
				ArrayList<String> wrongPortSpace = space.getWrongPortSpace();
				//#System.out.println("allowedSpace is " + allowedSpace);
				//#System.out.println("deniedSpace is " + deniedSpace);
				//先判断与NAT空间的交集，生成新的输入空间
				ArrayList<String> newInputSpace = space.copyNatOperations(newSecurity);
				//再判断与wrongPortSpace的交集，一旦有交集，这个路径就失效了

				//新的输入空间如果满足包含于旧的输入空间，则任何表项都不需要
				//与denied space的相交的空间，然后相交的空间与高优先级的允许空间对比，然后再添加表项
				//#System.out.println("newInputSpace is " + newInputSpace);
				//这个newInputSpace可能与就的输入空间securitySpace有交集,所以我们
				//计算出他们的交集，然后将securitySpace中去除掉交集的部分重新添加到newInputSpace上
				//SetOper.addNoRepeat(newInputSpace, newSecurity);			
				for(String inputSpace: newInputSpace) {
					//System.out.println("inputSpace is" + inputSpace);
					SetOper.addNoRepeat(newSecurity, inputSpace);
					//System.out.println("finish newSecurity is " + newSecurity);
				}
				
				ArrayList<String> nextInput = new ArrayList<String>();
				for(String strOne: newSecurity) {
					nextInput.add(strOne);
				}
				
				//最后产生的newInputSpace才是下一个Space真正的输入
				
				if(SetOper.interSectWithWrongPort(wrongPortSpace, newSecurity)) {
					flowNum = 1000;
					break;
				}
				//计算与denied空间的交集，如果交集全部都在highPriority空间中，那么只需要加上输入空间这条表项
				//可以证明这个交集的空间，内部一定是互不相交的
				
				ArrayList<String> interSectWithDenied = SetOper.interSect(newSecurity, deniedSpace);
				
				//判断与deniedSpace的交集是否在priority space里面，如果有交集，则计算出去除交集，也就是需要添加的空间
				ArrayList<String> spaceWithOutPrior = SetOper.noInterSect(highPrioSpace, interSectWithDenied);
				
				if(spaceWithOutPrior.size()!=0) {
					System.out.println("!!!!!!!!!!!!!!!!!需要添高优先级流表!!!!!!!!!!!!");
					System.out.println("高优先级流表为");
					System.out.println(spaceWithOutPrior);
					flowNum += spaceWithOutPrior.size();
					newSecurity = SetOper.removeInterFromList(newSecurity, interSectWithDenied);
				}
				
				if(newSecurity.size()!=0) {
					System.out.println("需要添加允许流表");
					System.out.println("旧的允许空间为");
					System.out.println(allowedSpace);
					System.out.println("如果添加的流表项已经存在于旧的允许空间，就不用添加了");
					System.out.println("新产生的允许空间表项为");
					System.out.println(newSecurity);
					flowNum += SetOper.addNoRepeatList(allowedSpace, newSecurity);
					//flowNum += newSecurity.size();
				}
				
				newSecurity = nextInput;
				
				//否则集合之外的还要添加一条highPriority的表项
/*				ArrayList<String> deniedResult = Util.computeIntersect(deniedSpace, securitySpace);
				if(deniedResult.size()==0) {
					System.out.println("no intersect");
				}else {
					flowNum++;
					//添加流表项，同时更新新的allowedSpace
					allowedSpace = Util.union(allowedSpace, deniedResult);
				}
				
				System.out.println("after conflict discover, allowedSpace is" + allowedSpace);
				System.err.println("flowNumber is " + flowNum);
				
				if(natSpace.equals("")) {
					continue;
				}
				String natResult = Util.computeIntersect(securitySpace, natSpace);
				if(natResult.contains("z")) {
					System.out.println("no intersect");
				}else {
					String newInputSpace = space.copyNatOperations(natResult);
					String interSect = Util.computeIntersect(newInputSpace, allowedSpace);
					if(interSect.contains("z")|| !interSect.equals(newInputSpace)) {
						//添加新的流表
						flowNum++;
						allowedSpace = Util.union(allowedSpace, newInputSpace);
					}
					//更新新的securitySpace
					securitySpace = Util.union(securitySpace, newInputSpace);
				}*/
			}
			//#totalFlows = totalFlows + flowNum;
		//#}
		//#return totalFlows;
			return flowNum;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}
}
