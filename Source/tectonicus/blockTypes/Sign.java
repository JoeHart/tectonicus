/*
 * Copyright (c) 2012-2014, John Campbell and other contributors.  All rights reserved.
 *
 * This file is part of Tectonicus. It is subject to the license terms in the LICENSE file found in
 * the top-level directory of this distribution.  The full list of project contributors is contained
 * in the AUTHORS file found in the same location.
 *
 */

package tectonicus.blockTypes;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import tectonicus.BlockContext;
import tectonicus.BlockType;
import tectonicus.BlockTypeRegistry;
import tectonicus.Chunk;
import tectonicus.TextLayout;
import tectonicus.configuration.LightFace;
import tectonicus.rasteriser.Mesh;
import tectonicus.rasteriser.SubMesh;
import tectonicus.rasteriser.SubMesh.Rotation;
import tectonicus.raw.RawChunk;
import tectonicus.raw.RawSign;
import tectonicus.renderer.Geometry;
import tectonicus.texture.SubTexture;

public class Sign implements BlockType
{
	private static final int WIDTH = 16;
	private static final int HEIGHT = 12;
	private static final int THICKNESS = 2;
	private static final int POST_THICKNESS = 2;
	private static final int POST_HEIGHT = 8;
	
	private final String name;
	
	private SubTexture frontTexture;
	private SubTexture sideTexture;
	private SubTexture edgeTexture;
	private SubTexture postTexture;
	
	private final boolean hasPost;
	
	public Sign(String name, SubTexture texture, final boolean hasPost)
	{
		this.name = name;
		this.hasPost = hasPost;
		
		final float texel;
		if (texture.texturePackVersion == "1.4")
			texel = 1.0f / 16.0f / 16.0f;
		else
			texel = 1.0f / 16.0f;
		
		this.frontTexture = new SubTexture(texture.texture, texture.u0, texture.v0, texture.u1, texture.v0+texel*HEIGHT);
		this.sideTexture = new SubTexture(texture.texture, texture.u0, texture.v0, texture.u1, texture.v0+texel*THICKNESS);
		this.edgeTexture = new SubTexture(texture.texture, texture.u0, texture.v0, texture.u0+texel*THICKNESS, texture.v0+texel*HEIGHT);
		this.postTexture = new SubTexture(texture.texture, texture.u0, texture.v0, texture.u0+texel*POST_THICKNESS, texture.v0+texel*POST_HEIGHT);
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
	public void addEdgeGeometry(final int x, final int y, final int z, BlockContext world, BlockTypeRegistry registry, RawChunk rawChunk, Geometry geometry)
	{
		final int data = rawChunk.getBlockData(x, y, z);
		
		SubMesh subMesh = new SubMesh();
		
		final float lightness = Chunk.getLight(world.getLightStyle(), LightFace.Top, rawChunk, x, y, z);
		
		Vector4f white = new Vector4f(lightness, lightness, lightness, 1);
		
		final float signBottom = hasPost ? 1.0f / 16.0f * POST_HEIGHT : 0;
		final float signDepth = hasPost ? 1.0f / 16.0f * 7 : 0;
		final float width = 1.0f / 16.0f * WIDTH;
		final float height = 1.0f / 16.0f * HEIGHT;
		final float thickness = 1.0f / 16.0f * THICKNESS;
		
		final float postHeight = 1.0f / 16.0f * POST_HEIGHT;
		final float postLeft = 1.0f / 16.0f * 7;
		final float postRight = 1.0f / 16.0f * 9;
		
		// Front
		subMesh.addQuad(new Vector3f(0, signBottom+height, signDepth+thickness), new Vector3f(width, signBottom+height, signDepth+thickness), new Vector3f(width, signBottom, signDepth+thickness), new Vector3f(0, signBottom, signDepth+thickness), white, frontTexture);
		
		// Back
		subMesh.addQuad(new Vector3f(width, signBottom+height, signDepth), new Vector3f(0, signBottom+height, signDepth), new Vector3f(0, signBottom, signDepth), new Vector3f(width, signBottom, signDepth), white, frontTexture);
		
		// Top
		subMesh.addQuad(new Vector3f(0, signBottom+height, signDepth), new Vector3f(width, signBottom+height, signDepth), new Vector3f(width, signBottom+height, signDepth+thickness), new Vector3f(0, signBottom+height, signDepth+thickness), white, sideTexture);
		
		// Left edge
		subMesh.addQuad(new Vector3f(0, signBottom+height, signDepth), new Vector3f(0, signBottom+height, signDepth+thickness), new Vector3f(0, signBottom, signDepth+thickness), new Vector3f(0, signBottom, signDepth), white, edgeTexture);
		
		// Right edge
		subMesh.addQuad(new Vector3f(width, signBottom+height, signDepth+thickness), new Vector3f(width, signBottom+height, signDepth), new Vector3f(width, signBottom, signDepth), new Vector3f(width, signBottom, signDepth+thickness), white, edgeTexture);
		
		final float xOffset = x;
		final float yOffset = y + (1.0f / 16.0f);
		final float zOffset = z;
		
		Rotation rotation = Rotation.None;
		float angle = 0;
		
		if (hasPost)
		{
			// Add a post
			
			// East face
			subMesh.addQuad(new Vector3f(postRight, postHeight, postLeft), new Vector3f(postLeft, postHeight, postLeft), new Vector3f(postLeft, 0, postLeft), new Vector3f(postRight, 0, postLeft), white, postTexture);
			
			// West face
			subMesh.addQuad(new Vector3f(postLeft, postHeight, postRight), new Vector3f(postRight, postHeight, postRight), new Vector3f(postRight, 0, postRight), new Vector3f(postLeft, 0, postRight), white, postTexture);
			
			// North face
			subMesh.addQuad(new Vector3f(postLeft, postHeight, postLeft), new Vector3f(postLeft, postHeight, postRight), new Vector3f(postLeft, 0, postRight), new Vector3f(postLeft, 0, postLeft), white, postTexture);
			
			// South face
			subMesh.addQuad(new Vector3f(postRight, postHeight, postRight), new Vector3f(postRight, postHeight, postLeft), new Vector3f(postRight, 0, postLeft), new Vector3f(postRight, 0, postRight), white, postTexture);
			
			rotation = Rotation.AntiClockwise;
			angle = 90 / 4.0f * data;
		}
		else
		{
			if (data == 2)
			{
				// Facing east
				rotation = Rotation.Clockwise;
				angle = 180;
			}
			else if (data == 3)
			{
				// Facing west
				// ...built this way
				
			}
			else if (data == 4)
			{
				// Facing north
				rotation = Rotation.AntiClockwise;
				angle = 90;
				
			}
			else if (data == 5)
			{
				rotation = Rotation.Clockwise;
				angle = 90;
			}
		}
		
		// Add the text
		
		for (RawSign s : rawChunk.getSigns())
		{
			if (s.localX == x && s.localY == y && s.localZ == z)
			{
				Mesh textMesh = geometry.getMesh(world.getTexturePack().getFont().getTexture(), Geometry.MeshType.AlphaTest);
				
				final float epsilon = 0.001f;
				
				final float lineHeight = 1.0f / 16.0f * 2.5f;
				
				TextLayout text1 = new TextLayout(world.getTexturePack().getFont());
				text1.setText(unescapeJava(s.text1), width/2f, signBottom+height - lineHeight * 1, signDepth+thickness+epsilon, true);
				
				TextLayout text2 = new TextLayout(world.getTexturePack().getFont());
				text2.setText(unescapeJava(s.text2), width/2f, signBottom+height - lineHeight * 2, signDepth+thickness+epsilon, true);
				
				TextLayout text3 = new TextLayout(world.getTexturePack().getFont());
				text3.setText(unescapeJava(s.text3), width/2f, signBottom+height - lineHeight * 3, signDepth+thickness+epsilon, true);
				
				TextLayout text4 = new TextLayout(world.getTexturePack().getFont());
				text4.setText(unescapeJava(s.text4), width/2f, signBottom+height - lineHeight * 4, signDepth+thickness+epsilon, true);
				
				text1.pushTo(textMesh, xOffset, yOffset, zOffset, rotation, angle);
				text2.pushTo(textMesh, xOffset, yOffset, zOffset, rotation, angle);
				text3.pushTo(textMesh, xOffset, yOffset, zOffset, rotation, angle);
				text4.pushTo(textMesh, xOffset, yOffset, zOffset, rotation, angle);
				
				break;
			}
		}
		
		subMesh.pushTo(geometry.getMesh(frontTexture.texture, Geometry.MeshType.Solid), xOffset, yOffset, zOffset, rotation, angle);
	}
	
}
