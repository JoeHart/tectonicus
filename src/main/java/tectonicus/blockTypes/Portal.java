/*
 * Copyright (c) 2020 Tectonicus contributors.  All rights reserved.
 *
 * This file is part of Tectonicus. It is subject to the license terms in the LICENSE file found in
 * the top-level directory of this distribution.  The full list of project contributors is contained
 * in the AUTHORS file found in the same location.
 *
 */

package tectonicus.blockTypes;

import java.util.Random;

import tectonicus.BlockContext;
import tectonicus.BlockType;
import tectonicus.BlockTypeRegistry;
import tectonicus.rasteriser.Mesh;
import tectonicus.raw.RawChunk;
import tectonicus.renderer.Geometry;
import tectonicus.texture.SubTexture;
import tectonicus.util.Colour4f;

public class Portal implements BlockType
{
	private final String name;
	
	private final SubTexture texture;
	
	public Portal(String name, SubTexture texture, int frame)
	{
		this.name = name;
		
		final int texHeight = texture.texture.getHeight();
		final int texWidth = texture.texture.getWidth();
		final int numTiles = texHeight/texWidth;
		
		if(numTiles > 1 && frame == 0)
		{
			Random rand = new Random();
			frame = rand.nextInt(numTiles)+1;
		}

		this.texture = new SubTexture(texture.texture, texture.u0, texture.v0+(float)(frame-1)*texWidth/texHeight, texture.u1, texture.v0+(float)frame*texWidth/texHeight);
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public boolean isSolid()
	{
		return false;
	}
	
	@Override
	public boolean isWater()
	{
		return false;
	}
	
	@Override
	public void addInteriorGeometry(int x, int y, int z, BlockContext world, BlockTypeRegistry registry, RawChunk rawChunk, Geometry geometry)
	{
		addEdgeGeometry(x, y, z, world, registry, rawChunk, geometry);
	}
	
	@Override
	public void addEdgeGeometry(int x, int y, int z, BlockContext world, BlockTypeRegistry registry, RawChunk chunk, Geometry geometry)
	{
		Mesh mesh = geometry.getMesh(texture.texture, Geometry.MeshType.Transparent);
		
		Colour4f colour = new Colour4f(1, 1, 1, 0.9f);
		
		BlockUtil.addTop(world, chunk, mesh, x, y, z, colour, texture, registry);
		
		BlockUtil.addNorth(world, chunk, mesh, x, y, z, colour, texture, registry);
		BlockUtil.addSouth(world, chunk, mesh, x, y, z, colour, texture, registry);
		BlockUtil.addEast(world, chunk, mesh, x, y, z, colour, texture, registry);
		BlockUtil.addWest(world, chunk, mesh, x, y, z, colour, texture, registry);
	}
}
