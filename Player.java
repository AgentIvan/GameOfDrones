import java.util.*;
import java.io.*;
import java.math.*;
import java.awt.Point;

/**
 * The main class.
 * A player has an unique argument : his strategy. Each turn, he'll apply his strategy.
 * 
 * @author Volodia
 */
class Player {
	
	Strategy strategy;
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player p = new Player();
		p.strategy = p.new Strategy(in);
		
		while (true) {
			p.strategy.applyStrategy(in);
			
		}
	}
	

	/**
	 * A drone, defined by : his position.
	 * 
	 * @author Volodia
	 *
	 */
	public class Drone {
		Point pos = new Point();
	}

	/**
	 * A team, defined by : A list of drones.
	 * 
	 * @author Volodia
	 *
	 */
	public class Team {
		Drone[] drones;
	}
	
	/**
	 * A zone, defined by : his position, his owner and an array of HashMaps dronesAtDistance.<br/><br/>
	 * The array contains with the index i the information about the distance between the drones of the team n� i and the zone.<br/>
	 * The key defined the distance (in turns) and the value is the list of all the drone of the team i that are at this distance in turns of the zone.
	 * 
	 * @author Volodia
	 */
	public class Zone {
		Point pos = new Point();
		int owner = -1;
		HashMap<Integer, List<Integer>>[] dronesAtDistance;
	}
	
	/**
	 * A few statics utility methods.
	 * 
	 * @author Volodia
	 *
	 */
	static class Utils {
		
		static int distance(Point depart, Point arrivee) {
			return (int) Math.sqrt( Math.pow(arrivee.x - depart.x, 2) + Math.pow(arrivee.y - depart.y, 2) );
		}
		
		static Point isoBarycentre(List<Point> points) {
			int x = 0, y = 0;
			for (Point point : points) {
				x += point.x;
				y += point.y;
			}
			
			x /= points.size();
			y /= points.size();
			
			return new Point(x, y);
		}
	}

	/**
	 * 
	 * Contains all the informations about the context
	 * 
	 * @author Volodia
	 *
	 */
	public class Context {
		
		
		int turnId = 0;
		
		int myTeamId;
		int nbOfTeams;
		int nbOfDrones;
		int nbOfZones;
		
		Team[] teams;
		Zone[] zones;

		/**
		 * Read the initial informations and initializes the context
		 * 
		 * @param in The scanner created in the main method
		 */
		@SuppressWarnings("unchecked")
		public Context(Scanner in) {
			nbOfTeams = in.nextInt();
			teams = new Team[nbOfTeams]; // P
			myTeamId = in.nextInt(); // I

			nbOfDrones = in.nextInt(); // D
			for (int i = 0; i < nbOfTeams; i++) {
				Drone[] drones = new Drone[nbOfDrones];
				for (int j=0; j<nbOfDrones; j++) {
					drones[j] = new Drone();
				}
				Team t = new Team();
				t.drones = drones;	
				teams[i] = t;
			}

			nbOfZones = in.nextInt(); // Z
			zones = new Zone[nbOfZones]; // Z

			for (int i = 0; i < nbOfZones; i++) {
				Zone z = new Zone();
				z.pos.x = in.nextInt();
				z.pos.y = in.nextInt();
				zones[i] = z;
			}

			// Initialisation des HashMaps de distances.
			for (Zone zone : zones) {
				zone.dronesAtDistance = new HashMap[nbOfTeams];
			}
			
		}
		
		/**
		 * Read the informations each turn and updates the context
		 * 
		 * @param in The scanner created in the main method
		 */
		public void updateContext(Scanner in) {
			turnId++;
			
			for (Zone z : zones) {
				z.owner = in.nextInt(); // update zones owner
			}

			for (int i = 0; i < nbOfTeams; i++) {
				Team t = teams[i];
				for (int j = 0; j < nbOfDrones; j++) {
					t.drones[j].pos = new Point(in.nextInt(), in.nextInt()); // update drones position
				}
			}

			for (Zone zone : zones) {
				for (int teamId=0; teamId<nbOfTeams; teamId++) {
					zone.dronesAtDistance[teamId] = new HashMap<Integer, List<Integer> >();
					for (int prevision = 0; prevision < 25; prevision++) {
						zone.dronesAtDistance[teamId].put(prevision, new ArrayList<Integer>());
					}
				}
			}
			
			for (Zone zone : zones) {
				for (int teamId=0; teamId<nbOfTeams; teamId++) {
					
					for (int droneId=0; droneId<nbOfDrones; droneId++) {
						int distanceInTurn = (Utils.distance(teams[teamId].drones[droneId].pos, zone.pos) - 1) / 100;
						
						if (distanceInTurn < 25) {
							zone.dronesAtDistance[teamId].get(distanceInTurn).add(droneId);
						}
					}
		
				}
			}

		}
		
		
	}
	
	
	/**
	 * The main intelligent class.
	 * All the strategies are defined in this class.
	 * 
	 * @author Volodia
	 *
	 */
	public class Strategy {

		int strategyId;
		Context context;
		Point defaultTarget;
		Point orders[];
		boolean isDroneInitialised[];
		int maxDefenders;
		
		List<Integer> targetsChoice = new ArrayList<Integer>();
		
		List<Integer> lastTargets = new ArrayList<Integer>();
		List<Integer> currentTargets = new ArrayList<Integer>();
		
		int necessaryDrones[];
		
		public void applyStrategy(Scanner in) {
			updateContext(in);
			play();
		}

		public void play() {
			resetOrders();
			
			System.err.println("----- GENERAL INFORMATIONS -----");
			System.err.println("Strategy id : " + strategyId);
			System.err.println("default target : " + defaultTarget.x + " " + defaultTarget.y);
			System.err.println("targetsChoice : " + targetsChoice.toString());
			System.err.println("last targets : " + lastTargets.toString());
			
			if (context.turnId < 15) {
				System.err.println("----- PHASE D'INITIALISATION -----");
				for (int droneId=0; droneId<context.nbOfDrones; ++droneId)
						initialiserDrone(droneId);
			}

			System.err.println("----- PHASE DEFENSIVE -----");
//			if (context.nbOfDrones == 3)
//				playDef3Drones();
//			else
				playDef();

			System.err.println("----- PHASE OFFENSIVE -----");
			if (strategyId == 2)
				playOff2();
			else
				playOff();
			

			sendOrders(context.myTeamId);
			
		}
		
		public void playDef() {
			for (int zoneId = 0; zoneId<context.nbOfZones; ++zoneId) {
				
				int defRadius = radius(zoneId);
				int classicRadius = classicRadius(zoneId);
				
				if (context.zones[zoneId].owner == context.myTeamId || (context.zones[zoneId].owner == -1 && context.nbOfTeams==2)) { // Pour chaque zone qui m'appartient ou qui est neutre quand on a 2 joueurs (à def aussi)
					if (isDefendable(zoneId, defRadius) && strongestEnnemyStrengthInZoneArea(zoneId, defRadius) <= maxDefenders) { // mais que je peux me défendre
							defZone(zoneId, defRadius);					
					} else if (isDefendable(zoneId, classicRadius)) { // On défend quand même
						if (strongestEnnemyStrengthInZoneArea(zoneId, classicRadius) > maxDefenders) {
							System.err.println("Trop de drônes à mobiliser. Abandon de la zone " + zoneId + " prochaine. Défense temporaire.");
							defZoneTemp(zoneId);
						} else {
							System.err.println("Trop de drônes à mobiliser. Abandon de la zone " + zoneId + " si insistance. Réduction du rayon de défense.");
							defZone(zoneId, classicRadius);
						}
					} else {
						System.err.println("Zone " + zoneId + " indéfendable en " + classicRadius + " tours");
						defZoneTemp(zoneId);
					}
					
				}
			}
		}
		
		void playDef3Drones() {
			for (int zoneId=0; zoneId<context.nbOfZones; zoneId++)  {
				List<Integer> freeDrones = freeDrones();
				if (context.zones[zoneId].owner == context.myTeamId && freeDrones.size() > 0) {
					System.err.println("Défense de la zone " + zoneId + " avec 1 drône uniquement");
					giveOrder(NearestDroneId(context.zones[zoneId].pos, context.myTeamId, freeDrones), zoneId);
				}
			}
		}
		
		void defZoneTemp(int zoneId) {
			if (isDefendable(zoneId, 1) && strongestEnnemyStrengthInZoneArea(zoneId, 1) <= maxDefenders)
				defZone(zoneId, 1);
			else 
				System.err.println("zone " + zoneId + "indéfendable en 1 tour");
		}
		
		public void playOff() {
			currentTargets.clear();
			
			List<Integer> myTargets = new ArrayList<Integer>();
			System.err.println("Looking for a target...");
			
			List<Integer> freeDrones = freeDrones();
			int targetId = findBestTarget(freeDrones, myTargets);
			
			while (targetId != -1) {
				System.err.println(freeDrones().size() + " free drones");
				System.err.println("Best target : Zone " + targetId);
				myTargets.add(targetId);
				
				fightTarget2(targetId, freeDrones);
				currentTargets.add(targetId);
				
				//updateNecessaryDrones();
				
				freeDrones = freeDrones();
				targetId = findBestTarget(freeDrones, myTargets);
			}
			
			System.err.println("No more target");
			lastTargets.clear();
			for (int currentTarget : currentTargets) {
				lastTargets.add(currentTarget);
			}
		}
		
		public void playOff2() {
			currentTargets.clear();
			
			List<Integer> myTargets = new ArrayList<Integer>();
			for (int i=0; i<context.nbOfZones; ++i)
				myTargets.add(i);
			for (int idToRemove : targetsChoice)
				myTargets.remove(myTargets.indexOf(idToRemove));
			
			
			System.err.println("Looking for a target...");
			
			List<Integer> freeDrones = freeDrones();
			int targetId = findBestTarget(freeDrones, myTargets);
			
			while (targetId != -1) {

				myTargets.add(targetId);
				
				if (targetsChoice.contains(targetId)) {
					System.err.println(freeDrones().size() + " free drones");
					System.err.println("Best target : Zone " + targetId);				
					fightTarget2(targetId, freeDrones);
					currentTargets.add(targetId);
				}
				
				updateNecessaryDrones();
				
				freeDrones = freeDrones();
				System.err.println("Looking for a target...");
				targetId = findBestTarget(freeDrones, myTargets);
			}
			
			System.err.println("No more target");
			lastTargets.clear();
			for (int currentTarget : currentTargets) {
				lastTargets.add(currentTarget);
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		void giveOrder(int idDrone, Point directionOrder) {
			if (idDrone >= 0 && idDrone < context.nbOfDrones) {
				Point myOrder = orders[idDrone];
				orders[idDrone] = (myOrder == null) ? directionOrder : myOrder;
				if (isInAZone(directionOrder))
					System.err.println("ORDRE : drone " + idDrone + " -> Zone " + zoneOfPoint(directionOrder));
				else
					System.err.println("ORDRE : drone " + idDrone + " -> coordonnées " + directionOrder.x + " " + directionOrder.y);
			}
		}
		
		void giveOrder(int idDrone, int zoneId) {
			if (idDrone >= 0 && idDrone < context.nbOfDrones) {
				Point directionOrder = inZoneButDirected(zoneId, defaultTarget);
				//if( Utils.distance(context.teams[context.myTeamId].drones[idDrone].pos, directionOrder) / 100 > Utils.distance(context.teams[context.myTeamId].drones[idDrone].pos, context.zones[zoneId].pos) / 100) {
				if( Utils.distance(context.teams[context.myTeamId].drones[idDrone].pos, directionOrder) / 100 > Utils.distance(context.teams[context.myTeamId].drones[idDrone].pos, inZoneButDirected(zoneId, context.teams[context.myTeamId].drones[idDrone].pos)) / 100) {
					System.err.println("drone " + idDrone + " ne peut pas aller vers la zone " + zoneId + " comme souhaité. Direction le centre");
					giveOrder(idDrone, context.zones[zoneId].pos);
				} else {
					giveOrder(idDrone, directionOrder);
				}
			}
		}

		public void sendOrders(int myTeamId) {
			System.err.println("----- SEND ORDERS -----");
			for (int i=0; i<context.nbOfDrones; ++i) {
				Point direction = orders[i];
				if (direction != null) {
					System.out.println(direction.x + " " + direction.y);
				}
				else {
					int closestFriendZoneId = closestFriendZoneId(context.teams[context.myTeamId].drones[i].pos);
					direction = closestFriendZoneId != -1 ? context.zones[closestFriendZoneId].pos : defaultTarget;
					System.out.println(direction.x + " " + direction.y);
					if (closestFriendZoneId != -1)
						System.err.println("drône " + i + " unused. Going to nearest friend zone : " + closestFriendZoneId );
					else
						System.err.println("drône " + i + " unused. Going to default target." );
				}
			}
		}
		
		
		void initialiserDrone(int droneId) {
			if (isInAZone(context.teams[context.myTeamId].drones[droneId].pos)  || isDroneInitialised[droneId] == true) {
				isDroneInitialised[droneId] = true;
				System.err.println("Initialisé : Drone " + droneId);
			} else {
				orders[droneId] = context.zones[closestZoneId(context.teams[context.myTeamId].drones[droneId].pos)].pos;
				System.err.println("En cours d'initialisation : Drone " + droneId);
			}
		}
		
		
		/*
		 * 
		 * GENERAL UTILITY METHODS
		 * 
		 */
		
		List<Integer> freeDrones() {
			List<Integer> result = new ArrayList<Integer>();
			
			for (int i=0; i<context.nbOfDrones; ++i)
				result.add(i);
			
			return freeDronesInList(result);
		}
		
		public int closestFriendZoneId(Point drone) {
			int distanceMin = 10000;
			int result = -1;
			for (int i=0; i<context.nbOfZones; ++i) {
				int currentDistance = Utils.distance(drone, context.zones[i].pos);
				if (currentDistance < distanceMin && context.zones[i].owner == context.myTeamId) {
					distanceMin = currentDistance;
					result = i;
				}
			}
			return result;
		}
		
		public int zoneOfPoint(Point point) {
			for (int i=0; i<context.nbOfZones; ++i) {
				if (Utils.distance(context.zones[i].pos, point) <= 100) {
					return i;
				}
			}
			return -1;
		}
		
		public Boolean isInAZone(Point drone) {
			if (zoneOfPoint(drone) != -1)
				return true;

			return false;
		}
		
		public Point isoBarycentreZones() {
			List<Point> points = new ArrayList<Point>();
			for (Zone zone : context.zones) {
				points.add(zone.pos);
			}
			return Utils.isoBarycentre(points);
		}
		
		public int closestZoneId(Point point) {
			int distanceMin = 10000;
			int result = 0;
			for (int i=0; i<context.nbOfZones; ++i) {
				int currentDistance = Utils.distance(point, context.zones[i].pos);
				if (currentDistance < distanceMin) {
					distanceMin = currentDistance;
					result = i;
				}
			}
			return result;
		}
		
		public Point inZoneButDirected(int zoneId, Point direction) {
			Point zone = context.zones[zoneId].pos;
			
			if (Utils.distance(zone, direction) <= 100) {
				return direction;
			}
			
			int x = zone.x + (95 * (direction.x - zone.x) / Utils.distance(zone, direction));
			int y = zone.y + (95 * (direction.y - zone.y) / Utils.distance(zone, direction));
			
			return new Point(x, y);
		}
		
		public Point inZoneAreaButDirected(int zoneId, int radius, Point direction) {
			Point zone = context.zones[zoneId].pos;
			
			if (Utils.distance(zone, direction) <= 100) {
				return direction;
			}
			
			int x = zone.x + ((95 + 100*radius) * (direction.x - zone.x) / Utils.distance(zone, direction));
			int y = zone.y + ((95 + 100*radius) * (direction.y - zone.y) / Utils.distance(zone, direction));
			
			return new Point(x, y);
		}
		
		List<Integer> zonesIdWithNearestBarycentre() {
			List<Integer> result = new ArrayList<Integer>();
			List<Point> points = new ArrayList<Point>();
			int distanceMinMoy = 10000; 
			
			for (int zoneId1=0; zoneId1<context.nbOfZones; zoneId1++) {
				for (int zoneId2=zoneId1+1; zoneId2<context.nbOfZones; zoneId2++) {
					for (int zoneId3=zoneId2+1; zoneId3<context.nbOfZones; zoneId3++) {
						for (int zoneId4=zoneId3+1; zoneId4<context.nbOfZones; zoneId4++) {
							points.clear();
							points.add(context.zones[zoneId1].pos);
							points.add(context.zones[zoneId2].pos);
							points.add(context.zones[zoneId3].pos);
							points.add(context.zones[zoneId4].pos);
							int distanceMoyTemp = distanceMoyBarycentre(points);
							if (distanceMinMoy > distanceMoyTemp && (estExtremiteX(zoneId1) || estExtremiteX(zoneId2) || estExtremiteX(zoneId3) || estExtremiteX(zoneId4))) {
								distanceMinMoy = distanceMoyTemp;
								result.clear();
								result.add(zoneId1);
								result.add(zoneId2);
								result.add(zoneId3);
								result.add(zoneId4);
							}
						}
					}
				}
			}		
			return result;
		}
		
		List<Integer> zones3IdWithNearestBarycentre() {
			List<Integer> result = new ArrayList<Integer>();
			List<Point> points = new ArrayList<Point>();
			int distanceMinMoy = 10000; 
			
			for (int zoneId1=0; zoneId1<context.nbOfZones; zoneId1++) {
				for (int zoneId2=zoneId1+1; zoneId2<context.nbOfZones; zoneId2++) {
					for (int zoneId3=zoneId2+1; zoneId3<context.nbOfZones; zoneId3++) {
						points.clear();
						points.add(context.zones[zoneId1].pos);
						points.add(context.zones[zoneId2].pos);
						points.add(context.zones[zoneId3].pos);
						int distanceMoyTemp = distanceMoyBarycentre(points);
						if (distanceMinMoy > distanceMoyTemp && (estExtremiteX(zoneId1) || estExtremiteX(zoneId2) || estExtremiteX(zoneId3))) {
							distanceMinMoy = distanceMoyTemp;
							result.clear();
							result.add(zoneId1);
							result.add(zoneId2);
							result.add(zoneId3);
						}	
					}
				}
			}		
			return result;
		}
		
		int distanceMoyBarycentre(List<Point> points) {
			int result = 0;
			Point barycentre = isoBarycentre(points);
			for (Point point : points) {
				result += Utils.distance(point, barycentre);
			}
			return (result / points.size());
		}
		
		boolean estExtremiteX(int zoneId) {
			boolean maxGauche = true;
			boolean maxDroite = true;
			for (Zone zone : context.zones) {
				if (zone.pos.x < context.zones[zoneId].pos.x) {
					maxGauche = false;
				}
				if (zone.pos.x > context.zones[zoneId].pos.x) {
					maxDroite = false;
				}
			}
			
			return (maxGauche || maxDroite);
		}
		
		Point isoBarycentre(List<Point> points) {
			int x = 0, y = 0;
			for (Point point : points) {
				x += point.x;
				y += point.y;
			}
			
			x /= points.size();
			y /= points.size();
			
			return new Point(x, y);
		}

		
		/* 
		 * *******************
		 * DEF UTILITY METHODS
		 * *******************
		 */
		
		void defZone(int zoneId, int defRadius) {
			
//			if (strongestEnnemyStrengthInZoneArea(zoneId, defRadius) > maxDefenders) {
//				System.err.println("Trop de drônes à mobiliser. Abandon de la zone " + zoneId);
//				return;
//			}
			
			System.err.println("défense de la zone" + zoneId + ". " +
					strongestEnnemyStrengthInZoneArea(zoneId, defRadius+1) + " drônes nécessaires avant "+defRadius+" tours.");
			

			int defendersMobilized = 0;
			for (int i=0; i<=defRadius; ++i) { // On défend pour chaque cercle
				
				for (int droneId : context.zones[zoneId].dronesAtDistance[context.myTeamId].get(i)) {
					if (defendersMobilized >= strongestEnnemyStrengthInZoneArea(zoneId, defRadius))
						continue;
					
					// Si j'ai pas encore mobilisé assez pour défendre mon cercle, j'envoie des renforts
					if (defendersMobilized < strongestEnnemyStrengthInZoneArea(zoneId, i)) {
						giveOrder(droneId, zoneId);
					} else {
						
						// Si j'ai pas encore mobilisé assez pour défendre le cercle suivant, je bouge pas pour pouvoir au cas où le faire
						if (defendersMobilized < strongestEnnemyStrengthInZoneArea(zoneId, i+1)) {
							// giveOrder(droneId, context.teams[context.myTeamId].drones[droneId].pos);
							giveOrder(droneId, inZoneAreaButDirected(zoneId, i, defaultTarget));
							
						// Sinon je m'éloigne puisque ça suffit pour défendre
						} else {
//							if (strat != 2)
								giveOrder(droneId, context.zones[closestZoneId(defaultTarget)].pos);
//							else
//								System.err.println(droneId + " est libre mais reste dans le coin");
						}
					}
					++defendersMobilized;
				}	
			}
			
		}
		
		
		// Radius d'une zone :
		// min(La distance de la plus proche zone que je contrôle / 2, la distance de la plus proche zone ennemi)
		int radius(int zoneId) {
			int result = 19;
			
			for (int otherZoneId=0; otherZoneId<context.nbOfZones; otherZoneId++) {
				if (otherZoneId != zoneId) {
					if (strategyId == 2 && targetsChoice.contains(zoneId)) {
						result = Utils.distance(context.zones[zoneId].pos, defaultTarget) / 100;
					} else {
						result = classicRadius(zoneId);
					}
				}
			}
			
			return Math.max(result, classicRadius(zoneId));
		}
		
		// def radius for any zone (also ennemy ones)
		int classicRadius(int zoneId) {
			int result = 19;
			int teamId = context.zones[zoneId].owner;
			
			for (int otherZoneId=0; otherZoneId<context.nbOfZones; otherZoneId++) {
				if (otherZoneId != zoneId) {
					if (teamId != -1 && context.zones[otherZoneId].owner == teamId) {
						result = ((Utils.distance(context.zones[zoneId].pos, context.zones[otherZoneId].pos)/100) - 2)/2 < result ? ((Utils.distance(context.zones[zoneId].pos, context.zones[otherZoneId].pos)/100) - 2)/2 : result;
					} else {
						result = ((Utils.distance(context.zones[zoneId].pos, context.zones[otherZoneId].pos)/100) - 2) < result ? ((Utils.distance(context.zones[zoneId].pos, context.zones[otherZoneId].pos)/100) - 2) : result;
					}
				}
			}
			
			return result;
		}
		
		boolean isDefendable(int zoneId, int nbTurns) {
			for (int i=0; i<=nbTurns; ++i)
				if ( strongestEnnemyStrengthInZoneArea(zoneId, i) > myFreeStrengthInZoneArea(zoneId, i) )
					return false;
			
			return true;
		}
		
		boolean isUndefendableEnnemyZone(int zoneId, int nbTurns) {
			for (int i=0; i<=nbTurns; ++i) {
				if (strongestEnnemyStrengthInZoneArea(zoneId, i) < myFreeStrengthInZoneArea(zoneId, i))
					return true;
			}
			
			return false;
		}
		
		int strongestEnnemyStrength(int zoneId) {
			int result = 0;
			
			for(int teamId=0; teamId<context.nbOfTeams; ++teamId) {
				if (teamId != context.myTeamId) {
					result = dronesInRadius(0, teamId, zoneId).size() > result ? dronesInRadius(0, teamId, zoneId).size() : result; 
				}
			}

			return result;
		}
		
		int strongestEnnemyStrengthInZoneArea(int zoneId, int radius) {
			int result = 0;
			
			for(int teamId=0; teamId<context.nbOfTeams; ++teamId) {
				if (teamId != context.myTeamId) {
					result = dronesInRadius(radius, teamId, zoneId).size() > result ? dronesInRadius(radius, teamId, zoneId).size() : result; 
				}
			}

			return result;
		}
		
		int strongestEnnemyIdInZoneArea(int zoneId, int radius) {
			int result = -1;
			
			
			for(int teamId=0; teamId<context.nbOfTeams; ++teamId) {
				if (teamId != context.myTeamId) {
					result = dronesInRadius(radius, teamId, zoneId).size() == strongestEnnemyStrengthInZoneArea(zoneId, radius) ? teamId : result; 
				}
			}

			return result;
		}

		int myStrength(int zoneId) {
			return dronesInRadius(0, context.myTeamId, zoneId).size();
		}
		
		int myStrengthInZoneArea(int zoneId, int radius) {
			return dronesInRadius(radius, context.myTeamId, zoneId).size();
		}
		
		int myFreeStrengthInZoneArea(int zoneId, int radius) {
			return freeDronesInList(dronesInRadius(radius, context.myTeamId, zoneId)).size();
		}
		
		List<Integer> dronesInRadius(int radiusInTurns, int teamId, int zoneId) {
			if (teamId == -1) {
				return dronesInRadius(radiusInTurns, strongestEnnemyIdInZoneArea(zoneId, radiusInTurns), zoneId);
			}
			
			List<Integer> result = new ArrayList<Integer>();
			
			for (int i=0; i<=radiusInTurns; ++i) {
				result.addAll(context.zones[zoneId].dronesAtDistance[teamId].get(i));
			}
			
			return result;
		}
		
		List<Integer> freeDronesInList(List<Integer> listOfDrones) {
			List<Integer> busyDrones = new ArrayList<Integer>();
			for (int droneId : listOfDrones) {
				if (isBusy(droneId)) {
					busyDrones.add(droneId);
				}
			}
			for (int droneId : busyDrones) {
				listOfDrones.remove(listOfDrones.indexOf(droneId));
			}
			
			return listOfDrones;
		}
		
		boolean isBusy(int droneId) {
			if (orders[droneId] == null)
				return false;
			return true;
		}

		
		/*
		 * 
		 * OFF UTILITY METHODS
		 * 
		 */
		
		int findBestTarget(List<Integer> freeDrones, List<Integer> butNotThisOnes) {
			List<Integer> undefendableEnnemyZones = new ArrayList<Integer>();
			for (int zoneId=0; zoneId<context.nbOfZones; zoneId++) {
				if (context.zones[zoneId].owner != context.myTeamId && !butNotThisOnes.contains(zoneId) && isUndefendableEnnemyZone(zoneId, 19)) {
					undefendableEnnemyZones.add(zoneId);
				}
			}
			if (!undefendableEnnemyZones.isEmpty()) {
				int bestTarget = bestTarget2(undefendableEnnemyZones, freeDrones);
				System.err.println("Cette zone est indéfendable par l'ennemi.");
				return bestTarget;
			}
			
			
			List<Integer> weakestEnnemyZonesInTheirArea = findWeakestEnnemyZonesInTheirArea(butNotThisOnes);
			if (!weakestEnnemyZonesInTheirArea.isEmpty()) {
				return bestTarget2(weakestEnnemyZonesInTheirArea, freeDrones);
			}
			
			return bestTarget2(findWeakestEnnemyZones(butNotThisOnes), freeDrones);
		}

		List<Integer> findWeakestEnnemyZonesInTheirArea(List<Integer> butNotThosesOnes) {
			List<Integer> result = new ArrayList<Integer>();
			
			int dronesInDefRadius = 100;
			
			for (int i=0; i<context.nbOfZones; ++i) {
				if (context.zones[i].owner != context.myTeamId && !butNotThosesOnes.contains(i)) { // Pour les zones ennemies données (ie pas les miennes ni celles pass�es en argument)
					if (strongestEnnemyStrengthInZoneArea(i, classicRadius(i)) == dronesInDefRadius) {
						// if (dronesInRadius(classicRadius(i), context.zones[i].owner, i).size() == dronesInDefRadius) {
						// maybe strongestEnnemyStrengthInZoneArea(i, classicRadius(i)) was better
						result.add(i);
						// } else if (dronesInRadius(classicRadius(i), context.zones[i].owner, i).size() < dronesInDefRadius) {
					} else if (strongestEnnemyStrengthInZoneArea(i, classicRadius(i)) < dronesInDefRadius) {
						result.clear();
						result.add(i);
						dronesInDefRadius = strongestEnnemyStrengthInZoneArea(i, classicRadius(i));
					}
				}
			}
			
			return result;
		}
		
		List<Integer> findWeakestEnnemyZones(List<Integer> butNotThosesOnes) {
			List<Integer> result = new ArrayList<Integer>();
			int necessaryDronesTemp = 100;
			
			for (int i=0; i<context.nbOfZones; ++i) {
				if (context.zones[i].owner != context.myTeamId && !butNotThosesOnes.contains(i)) { // Pour les zones ennemies donn�es (ie pas les miennes ni celles pass�es en argument)
					if (necessaryDrones[i] == necessaryDronesTemp) {
						result.add(i);
					} else if (necessaryDrones[i] < necessaryDronesTemp) {
						result.clear();
						result.add(i);
						necessaryDronesTemp = necessaryDrones[i];
					}
				}
			}
			
			return result;
		}
		
		int bestTarget(List<Integer> targetsChoiceToSelect, List<Integer> freeDrones) {
			int result = -1;
			
			if (targetsChoiceToSelect.isEmpty() || necessaryDrones[targetsChoiceToSelect.get(0)]+context.zones[targetsChoiceToSelect.get(0)].dronesAtDistance[context.myTeamId].get(0).size() > freeDrones.size() )
				return result;
			
			int distanceMinToGo = 100;
			for (int targetId : targetsChoiceToSelect) {
				int fighterCount = 0;
				int currentDistanceToGo = 16;
				
				for(int i=1; i<=19; ++i) {
					fighterCount += context.zones[targetId].dronesAtDistance[context.myTeamId].get(i).size();
					if (fighterCount >= necessaryDrones[targetId]) {
						currentDistanceToGo = i;
						break;
					}
				}
				
				if (currentDistanceToGo < distanceMinToGo) {
					result = targetId;
					distanceMinToGo = currentDistanceToGo;
				}
			}
			
			if (strategyId==2) {
				result = moreLonelyZoneId(targetsChoiceToSelect);
			}
			
			
			if ( necessaryDrones[result] + context.zones[result].dronesAtDistance[context.myTeamId].get(0).size() > freeDrones.size() ) {
				System.err.println("ATTENTION, NORMALEMENT SI ON EST LA C'EST QUE Y'A UNE ERREUR PLUS HAUT !");
				return -1;
			}
			
			System.err.println("best target found : " + result + 
					". necessary " + necessaryDrones[result] + ". present " + 
					context.zones[result].dronesAtDistance[context.myTeamId].get(0).size());
			
			return result;
		}
		
		int bestTarget2 (List<Integer> targetsChoiceToSelect, List<Integer> freeDrones) {
			int result = -1;
			
			List<Integer> newTargetsChoice = new ArrayList<Integer>();
			for (int target : lastTargets) {
				if (targetsChoiceToSelect.contains(target)) {
					newTargetsChoice.add(target);
				}
			}
			if (!newTargetsChoice.isEmpty()) {
				targetsChoiceToSelect = newTargetsChoice;
			}
			
			if ( targetsChoiceToSelect.isEmpty() )
				return -1;
			
			
			int timeMinToGo = 100;
			for (int targetId : targetsChoiceToSelect) {
				int timeToGoTemp = timeToGetTarget(targetId);
				if (timeToGoTemp < timeMinToGo) {
					result = targetId;
					timeMinToGo = timeToGoTemp;
				}
			}
			if (result != -1 && strongestEnnemyStrengthInZoneArea(result, timeMinToGo) <= freeDrones.size()) {
				System.err.println("Attaque zone " + result + ". Prise de façon certaine en " + timeMinToGo + " tours.");
				
				return result;
			}
			
			int distanceMinToGo = 100;
			for (int targetId : targetsChoiceToSelect) {
				int fighterCount = 0;
				int currentDistanceToGo = 16;
				
				for(int i=1; i<=19; ++i) {
					fighterCount += context.zones[targetId].dronesAtDistance[context.myTeamId].get(i).size();
					if (fighterCount >= strongestEnnemyStrengthInZoneArea(targetsChoiceToSelect.get(0), classicRadius(targetsChoiceToSelect.get(0)))+1) {
						currentDistanceToGo = i;
						break;
					}
				}
				
				if (currentDistanceToGo < distanceMinToGo) {
					result = targetId;
					distanceMinToGo = currentDistanceToGo;
				}
			}
			
			if (strategyId==2) {
				result = moreLonelyZoneId(targetsChoiceToSelect);
			}
			
			if (result == -1) {
				System.err.println("Aucune cible trouvée.");
				return result;
			}
			
			if (strongestEnnemyStrengthInZoneArea(result, classicRadius(result)) > freeDrones.size()) {
				return -1;
			}
			
			System.err.println("best target found : " + result + 
					". necessary " + (strongestEnnemyStrengthInZoneArea(targetsChoiceToSelect.get(targetsChoiceToSelect.indexOf(result)), classicRadius(targetsChoiceToSelect.get(targetsChoiceToSelect.indexOf(result))))+1) +
					" dans un rayon de " + classicRadius(targetsChoiceToSelect.get(targetsChoiceToSelect.indexOf(result))) + " tours.");
			
			return result;
		}
		
		// Return the time (in turns) we need to get a target with our free drones.
		int timeToGetTarget(int zoneId) {
			int ennemyStrengh = strongestEnnemyStrength(zoneId);
			int myStrengh = myStrength(zoneId);
			
			for (int turnNb=0; turnNb <= classicRadius(zoneId); turnNb++) {
				ennemyStrengh = strongestEnnemyStrengthInZoneArea(zoneId, turnNb);
				myStrengh = myFreeStrengthInZoneArea(zoneId, turnNb);
				if (ennemyStrengh < myStrengh) {
					return turnNb;
				}
			}
			
			return 1000;
		}
		
		
		int dronesToGetTarget(int zoneId) {
			int timeToGetTarget = timeToGetTarget(zoneId);
			
			if (timeToGetTarget == 1000)
				return strongestEnnemyStrengthInZoneArea(zoneId, classicRadius(zoneId)) + 1;
			else
				return strongestEnnemyStrengthInZoneArea(zoneId, timeToGetTarget) + 1;
		}
		
		int moreLonelyZoneId(List<Integer> zoneIds) {
			int result = -1;
			int DistanceMoy = 0;
			
			for (int zoneId : zoneIds) {
				int currentDistanceMoy = distanceMoy(zoneId);
				if (currentDistanceMoy > DistanceMoy) {
					result = zoneId;
					DistanceMoy = currentDistanceMoy;
				}
			}
			
			return result;
		}
		
		void fightTarget(int target, List<Integer> freeDrones) {
			for (int i=0; i< necessaryDrones[target] + context.zones[target].dronesAtDistance[context.myTeamId].get(0).size(); ++i) {
				int fighter = NearestDroneId(context.zones[target].pos, context.myTeamId, freeDrones);
				giveOrder(fighter, target);
				freeDrones.remove(freeDrones.indexOf(fighter));
			}
		}
		
		void fightTarget2(int target, List<Integer> freeDrones) {
			int nbOfFreeDrones = freeDrones.size();
			for (int i=0; i< Math.min(dronesToGetTarget(target), nbOfFreeDrones); ++i) {
				int fighter = NearestDroneId(context.zones[target].pos, context.myTeamId, freeDrones);
				giveOrder(fighter, target);
				freeDrones.remove(freeDrones.indexOf(fighter));
			}
		}
		
		
		int NearestDroneId(Point target, int teamId, List<Integer> freeDrones) {
			int distanceMin = 10000;
			int result = -1;
			Team t = context.teams[teamId];
			for (int i=0; i<context.nbOfDrones; ++i) {
				int currentDistance = Utils.distance(t.drones[i].pos, target);
				if (currentDistance < distanceMin && freeDrones.contains(i)) {
					distanceMin = currentDistance;
					result = i;
				}
			}
			return result;
		}
		
		int distanceMoy(int zoneId) {
			int result = 0;
			for (int i=0; i<context.nbOfZones; ++i) {
				if (i != zoneId) {
					result += Utils.distance(context.zones[zoneId].pos, context.zones[i].pos);
				}
			}
			return result/context.nbOfZones;
		}
		
		void updateNecessaryDrones() {
			List<Integer> busyDrones = busyDrones();
			
			for (int droneId : busyDrones) {
				// Si un dr�ne occup� est dans une zone
				// Et que sa target n'est pas cette zone l� (distance > 100)
				// Il va bouger, donc on ajoute un dr�ne n�cessaire pour la zone en question
				if ( isInAZone(context.teams[context.myTeamId].drones[droneId].pos) && Utils.distance(orders[droneId], context.teams[context.myTeamId].drones[droneId].pos ) > 100 ) {
					necessaryDrones[zoneOfPoint(context.teams[context.myTeamId].drones[droneId].pos)]++;
				}
			}
		}
		
		List<Integer> busyDrones() {
			List<Integer> result = new ArrayList<Integer>();
			
			for (int i=0; i<context.nbOfDrones; ++i)
				result.add(i);
			
			return busyDronesInList(result);
		}
		
		List<Integer> busyDronesInList(List<Integer> listOfDrones) {
			List<Integer> freeDrones = new ArrayList<Integer>();
			for (int droneId : listOfDrones) {
				if (!isBusy(droneId)) {
					freeDrones.add(droneId);
				}
			}
			for (int droneId : freeDrones) {
				listOfDrones.remove(listOfDrones.indexOf(droneId));
			}
			
			return listOfDrones;
		}
		
		
		
		
		
		
		
		
		
		
		// Constructor
		public Strategy(Scanner in) {
			this.context = new Context(in);

			initStrategy();
		}

		public void initStrategy() {
			isDroneInitialised = new boolean[context.nbOfDrones];
			for (int droneId=0; droneId<context.nbOfDrones; droneId++) {
				isDroneInitialised[droneId] = false;
			}
			
			necessaryDrones = new int[context.nbOfZones];
			
			// Initialisation du nombre maximum de d�fendeurs autoris�s sur une zone.
			maxDefenders = Math.max(3, context.nbOfDrones/2 + 1);
			
			if (context.nbOfTeams > 2 && context.nbOfDrones > 3) {
				this.strategyId = 2;
				// Strat 2 : We focus on a group of zones
				if (context.nbOfTeams == 4)
					targetsChoice = zonesIdWithNearestBarycentre();
				else
					targetsChoice = zones3IdWithNearestBarycentre();
			} else {
				this.strategyId = 1;
				// Strat 1 : We can attack any zone
				for (int zoneId=0; zoneId<context.nbOfZones; zoneId++) {
					targetsChoice.add(zoneId);
				}
			}
			
			List<Point> points = new ArrayList<Point>();
			for (int zoneId : targetsChoice) {
				points.add(context.zones[zoneId].pos);
			}
			
			defaultTarget = isoBarycentre(points);
		}
		
		public void updateContext(Scanner in) {
			this.context.updateContext(in);
			redefineStrategy();
		}

		public void redefineStrategy() {		
			// ICI INITIALISATION DE NECESSARY DRONES
			for (int i=0; i<context.nbOfZones; ++i) {
				int myStrength = myStrength(i);
				int strongestEnnemyStrength = strongestEnnemyStrength(i);
				if (context.zones[i].owner == context.myTeamId) { // Si la zone m'appartient ou est neutre, il m'en faut minimum autant que le plus fort
					necessaryDrones[i] = (strongestEnnemyStrength - myStrength) == 0 ? 0 : (strongestEnnemyStrength - myStrength);
				} else { // Si la zone ne m'appartient pas, il m'en faut minimum un de plus que le plus fort
					necessaryDrones[i] = (strongestEnnemyStrength - myStrength) == 0 ? 1 : (strongestEnnemyStrength - myStrength + 1);
				}
			}
			
		}

		public void resetOrders() {
			orders = new Point[context.nbOfDrones];
		}

	}



}