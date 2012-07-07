package me.ftbastler.BukkitGames;

public class Border {
	int radiusSq;
	int definiteSq;
	double centerX;
	double centerZ;
	double radius;

	public Border(double X, double Z, double Radius) {
		this.centerX = X;
		this.centerZ = Z;
		this.radius = Radius;
		this.radiusSq = (int) (this.radius * this.radius);
		this.definiteSq = (int) Math.sqrt(0.5D * this.radiusSq);
	}

	public String toString() {
		return "X: " + this.centerX + " Z: " + this.centerZ + " Radius: "
				+ this.radius;
	}
}