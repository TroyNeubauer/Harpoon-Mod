package com.troy.tco.util;

import net.minecraft.util.math.Vec3d;

public class MathUtils
{
	public static Vec3d clampPointToLine(Vec3d pointToClamp, Vec3d lineStart, Vec3d lineEnd)
	{
		double minX, minY, minZ, maxX, maxY, maxZ;
		if(lineStart.x <= lineEnd.x)
		{
			minX = lineStart.x;
			maxX = lineEnd.x;
		}
		else
		{
			minX = lineEnd.x;
			maxX = lineStart.x;
		}
		if (lineStart.y <= lineEnd.y)
		{
			minY = lineStart.y;
			maxY = lineEnd.y;
		}
		else
		{
			minY = lineEnd.y;
			maxY = lineStart.y;
		}
		if (lineStart.z <= lineEnd.z)
		{
			minZ = lineStart.z;
			maxZ = lineEnd.z;
		}
		else
		{
			minZ = lineEnd.z;
			maxZ = lineStart.z;
		}
		return new Vec3d((pointToClamp.x < minX) ? minX : Math.min(pointToClamp.x, maxX),
				(pointToClamp.y < minY) ? minY : Math.min(pointToClamp.y, maxY),
				(pointToClamp.z < minZ) ? minZ : Math.min(pointToClamp.z, maxZ));
	}

	//Returns the shortest distance between 2 line segments in 3d space
	public static double distBetweenLines(Vec3d l1Start, Vec3d l1End, Vec3d l2Start, Vec3d l2End)
	{
		long start = System.nanoTime();
		Vec3d p1, p2, p3, p4, d1, d2;
		p1 = l1Start;
		p2 = l1End;
		p3 = l2Start;
		p4 = l2End;
		d1 = p2.subtract(p1);
		d2 = p4.subtract(p3);
		double eq1nCoeff = (d1.x * d2.x) + (d1.y * d2.y) + (d1.z * d2.z);
		double eq1mCoeff = (-(Math.pow(d1.x, 2)) - (Math.pow(d1.y, 2)) - (Math.pow(d1.z, 2)));
		double eq1Const = ((d1.x * p3.x) - (d1.x * p1.x) + (d1.y * p3.y) - (d1.y * p1.y) + (d1.z * p3.z) - (d1.z * p1.z));
		double eq2nCoeff = ((Math.pow(d2.x, 2)) + (Math.pow(d2.y, 2)) + (Math.pow(d2.z, 2)));
		double eq2mCoeff = -(d1.x * d2.x) - (d1.y * d2.y) - (d1.z * d2.z);
		double eq2Const = ((d2.x * p3.x) - (d2.x * p1.x) + (d2.y * p3.y) - (d2.y * p2.y) + (d2.z * p3.z) - (d2.z * p1.z));
		double[][] M = new double[][] { { eq1nCoeff, eq1mCoeff, -eq1Const }, { eq2nCoeff, eq2mCoeff, -eq2Const } };
		int rowCount = M.length;
		// pivoting
		for (int col = 0; col + 1 < rowCount; col++) {
			if (M[col][col] == 0)
			// check for zero coefficients
			{
				// find non-zero coefficient
				int swapRow = col + 1;
				for (; swapRow < rowCount; swapRow++)
				{
					if (M[swapRow][col] !=0)
						break;
				}

				if (M[swapRow][col] != 0) // found a non-zero coefficient?
				{
					// yes, then swap it with the above
					double[] tmp = new double[rowCount + 1];
					for (int i = 0; i < rowCount + 1; i++) {
						tmp[i] = M[swapRow][i];
						M[swapRow][i] = M[col][i];
						M[col][i] = tmp[i];
					}
				} else return -1.0; // no, then the matrix has no unique solution
			}
		}

		// elimination
		for (int sourceRow = 0; sourceRow + 1 < rowCount; sourceRow++)
		{
			for (int destRow = sourceRow + 1; destRow < rowCount; destRow++)
			{
				double df = M[sourceRow][sourceRow];
				double sf = M[destRow][sourceRow];
				for (int i = 0; i < rowCount + 1; i++)
					M[destRow][i] = M[destRow][i] * df - M[sourceRow][i] * sf;
			}
		}

		// back-insertion
		for (int row = rowCount - 1; row >= 0; row--)
		{
			double f = M[row][row];
			if (f == 0) return -1.0;

			for (int i = 0; i < rowCount + 1; i++)
				M[row][i] /= f;
			for (int destRow = 0; destRow < row; destRow++)
			{
				M[destRow][rowCount] -= M[destRow][row] * M[row][rowCount]; M[destRow][row] = 0;
			}
		}
		double n = M[0][2];
		double m = M[1][2];
		Vec3d i1 = new Vec3d(p1.x + (m * d1.x), p1.y + (m * d1.y), p1.z + (m * d1.z));
		Vec3d i2 = new Vec3d(p3.x + (n * d2.x), p3.y + (n * d2.y), p3.z + (n * d2.z));
		Vec3d i1Clamped = clampPointToLine(i1, l1Start, l1End);
		Vec3d i2Clamped = clampPointToLine(i2, l2Start, l2End);
		double result = i1Clamped.subtract(i2Clamped).lengthVector();
		//System.out.println("Solving min-distance line equation took " + ((System.nanoTime() - start) / 1000.0) + " micro seconds");
		return result;
	}

}
