/*
 * Copyright (c) 2020 Tectonicus contributors.  All rights reserved.
 *
 * This file is part of Tectonicus. It is subject to the license terms in the LICENSE file found in
 * the top-level directory of this distribution.  The full list of project contributors is contained
 * in the AUTHORS file found in the same location.
 *
 */

package tectonicus.texture;

import lombok.extern.log4j.Log4j2;
import tectonicus.Version;
import tectonicus.rasteriser.Rasteriser;
import tectonicus.rasteriser.Texture;
import tectonicus.rasteriser.TextureFilter;
import tectonicus.renderer.Font;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tectonicus.Version.UNKNOWN_VERSION;
import static tectonicus.Version.VERSIONS_6_TO_8;
import static tectonicus.Version.VERSIONS_9_TO_11;
import static tectonicus.Version.VERSION_12;
import static tectonicus.Version.VERSION_13;
import static tectonicus.Version.VERSION_14;
import static tectonicus.Version.VERSION_4;
import static tectonicus.Version.VERSION_5;
import static tectonicus.Version.VERSION_RV;

@Log4j2
public class TexturePack
{
	private Version version;
	
	private Rasteriser rasteriser;
	
	private Texture vignetteTexture;
	
	private BufferedImage itemSheet;
	private BufferedImage iconSheet;
	private BufferedImage chestImage;
	
	private BufferedImage grassLookupImage;
	private BufferedImage foliageLookupImage;
	
	private Font font;
	
	private ZipStack zipStack;
	
	private Map<String, PackTexture> loadedPackTextures;
	
	public TexturePack(Rasteriser rasteriser, File minecraftJar, File texturePack, List<File> modJars)
	{
		if (!minecraftJar.exists())
			throw new RuntimeException("Couldn't find minecraft.jar at "+minecraftJar.getAbsolutePath());
	
		this.rasteriser = rasteriser;
		
		loadedPackTextures = new HashMap<>();
		
		try
		{
			zipStack = new ZipStack(minecraftJar, texturePack, modJars);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Couldn't open jar files for texture reading", e);
		}

		//TODO: Clean up this version stuff
		if (zipStack.hasFile("assets/minecraft/textures/block/bamboo_stalk.png")) {
			version = VERSION_14;
		} else if (zipStack.hasFile("assets/minecraft/textures/block/acacia_door_bottom.png")) {
			version = VERSION_13;
		} else if (zipStack.hasFile("assets/minecraft/textures/blocks/concrete_lime.png")) {
			version = VERSION_12;
		} else if (zipStack.hasFile("assets/minecraft/textures/blocks/bed_head_top.png")) {
			version = VERSIONS_9_TO_11;
		} else if (zipStack.hasFile("assets/minecraft/textures/blocks/usb_charger_side.png")) {
			version = VERSION_RV;
		} else if (zipStack.hasFile("assets/minecraft/textures/blocks/redstone_dust_cross.png")) {
			version = VERSIONS_6_TO_8;
		} else if (zipStack.hasFile("textures/blocks/activatorRail.png")) {
			version = VERSION_5;
		} else if (zipStack.hasFile("terrain.png")) {
			version = VERSION_4;
		} else {
			version = UNKNOWN_VERSION;
		}

		System.out.println("Texture pack version: " + version);

		try
		{
			if (version == VERSION_4)
				findTexture("terrain.png[0, 0]");
			else if (version == VERSION_5)
			{
				// Load each individual texture file?
			}
			
		/*	ZipStackEntry terrainEntry = zipStack.getEntry("terrain.png");
			if (terrainEntry != null)
			{
				BufferedImage terrainImage = copy( ImageIO.read(terrainEntry.getInputStream()) );
				
				BufferedImage[] mipmaps = PackTexture.generateTileMips(terrainImage);
				
				terrainTexture = rasteriser.createTexture(mipmaps, TextureFilter.NEAREST);
				
				final float tileU = 1.0f / 16.0f;
				final float tileV = 1.0f / 16.0f;
				
				final float uNudge = tileU / 64.0f;
				final float vNudge = tileV / 64.0f;
				
				for (int tileX=0; tileX<16; tileX++)
				{
					for (int tileY=0; tileY<16; tileY++)
					{
						final float u0 = tileX * tileU + uNudge;
						final float u1 = (tileX+1) * tileU - uNudge;
						
						final float v0 = tileY * tileV + vNudge;
						final float v1 = (tileY+1) * tileV - vNudge;
						
						SubTexture sub = new SubTexture(terrainTexture, u0, v0, u1, v1);
						tiles.put(new Point(tileX, tileY), sub);
					}
				}
			}
			else
			{
				ZipStackEntry serverClass = zipStack.getEntry("net/minecraft/server/MinecraftServer.class");
				final boolean isServerJar = serverClass != null;
				
				ZipStackEntry launcherClass = zipStack.getEntry("net/minecraft/MinecraftLauncher.class");
				final boolean isLauncherJar = launcherClass != null;
				
				if (isServerJar)
				{
					throw new RuntimeException("Couldn't find terrain.png in "+minecraftJar.getName()+", make sure you're using the minecraft client jar, not the server jar!");
				}
				else if (isLauncherJar)
				{
					throw new RuntimeException("Couldn't find terrain.png in "+minecraftJar.getName()+", make sure you're using the minecraft client jar, not the client launcher jar!");
				}
				else
				{
					throw new RuntimeException("Couldn't find terrain.png in "+formatPaths(minecraftJar, texturePack));
				}
			}
		*/
			String path;
			if(version.getNumVersion() >= VERSIONS_6_TO_8.getNumVersion())
				path = "assets/minecraft/textures/";
			else
				path = "";
			
			try {
				BufferedImage vignetteImage = copy( ImageIO.read(zipStack.getStream(path + "misc/vignette.png")) );
				vignetteTexture = rasteriser.createTexture(vignetteImage, TextureFilter.LINEAR);
			} catch (Exception e) {
				
			}

			//TODO: try this alternate way of loading these images
//			ZipStackEntry vignetteEntry = zipStack.getEntry(path + "misc/vignette.png");
//			if (vignetteEntry != null)
//			{
//				BufferedImage vignetteImage = copy( ImageIO.read(vignetteEntry.getInputStream()) );
//				vignetteTexture = rasteriser.createTexture(vignetteImage, TextureFilter.LINEAR);
//			}
			
			loadBedTextures();
			loadShulkerTextures();

			if (version.getNumVersion() >= VERSION_14.getNumVersion()) {
				loadPaintingTextures();
			}

			if (version == VERSION_4) {
				try {
					itemSheet = copy(ImageIO.read(zipStack.getStream(path + "gui/items.png")));
				} catch (IllegalArgumentException e) {
					System.out.println("Could not find items.png.  This is only required if using a Minecraft 1.4 or older jar file.");
				}
			}

			try {
				iconSheet = copy( ImageIO.read( zipStack.getStream(path + "gui/icons.png") ) );
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't find icons.png in "+formatPaths(minecraftJar, texturePack));
			}
			
			try {
				InputStream imgStream = zipStack.getStream(path + "gui/container.png");
				if (imgStream == null)
					imgStream = zipStack.getStream(path + "gui/container/generic_54.png");
				chestImage = copy( ImageIO.read( imgStream ) );
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't find generic_54.png in "+formatPaths(minecraftJar, texturePack));
			}

			try {
				InputStream imgStream = zipStack.getStream(path + "misc/grasscolor.png");
				if (imgStream == null)
					imgStream = zipStack.getStream(path + "colormap/grass.png");
				grassLookupImage = copy( ImageIO.read( imgStream ) );
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't find grasscolor.png in "+formatPaths(minecraftJar, texturePack));
			}
			
			try {
				InputStream imgStream = zipStack.getStream(path + "misc/foliagecolor.png");
				if (imgStream == null)
					imgStream = zipStack.getStream(path + "colormap/foliage.png");
				foliageLookupImage = copy( ImageIO.read( imgStream ) );
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't find foliagecolor.png in "+formatPaths(minecraftJar, texturePack));
			}
			
			//TODO: The font stuff needs some work
			try {
				InputStream imgStream = zipStack.getStream(path + "font/default.png");
				if (imgStream == null)
					imgStream = zipStack.getStream(path + "font/ascii.png");
				InputStream textIn = this.getClass().getResourceAsStream("/font.txt");
				BufferedImage fontSheet = ImageIO.read( imgStream );
				font = new Font(rasteriser, fontSheet, textIn);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't find font resources in "+formatPaths(minecraftJar, texturePack));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Couldn't load textures from "+formatPaths(minecraftJar, texturePack), e);
		}
	}
	
	private String formatPaths(File first, File second)
	{
		String result = "";
		
		if (first != null)
			result += first.getAbsolutePath();
		
		result += " ";
		
		if (second != null)
			result += second.getAbsolutePath();
		
		return result.trim();
	}
	
	// TODO: refactor to remove this? why is it still needed?
	public Texture getTexture()
	{
	//	return terrainTexture;
		/*if (this.version == "1.4")
			return findTexture("terrain").texture;
		else*/
			return null;
	}

	public PackTexture getTexture(String path) {
		return loadedPackTextures.get(path);
	}
	
	public SubTexture findTexture(String texturePath)
	{
		SubTexture result = null;
		
		TextureRequest request = parseRequest(texturePath);
		
		// First see if we already have it loaded
	//	result = loadedSubTextures.get(request);
		
		// If not already loaded, then load it and cache it
	//	if (result != null)
		{
			PackTexture tex = findTexture(request); // find existing or load
			
			result = tex.find(request, version); // find existing or load
			assert (result != null);
		//	loadedSubTextures.put(request, result);
		}
		
		return result;
	}
	
	private TextureRequest parseRequest(String texturePath)
	{
		// texture path could be:
		// terrain[0, 1]
		// terrain(0, 1, 2, 3)
		// path/file.ext
		// path/file.ext[0, 1]
		// path/file.ext(0, 1, 2, 3)
		
		// Minecraft 1.5
		// texture path could be:
		// file.ext  -assume it's located in textures/blocks/
		// path/file.ext
		// path/file.ext[0, 1]
		// path/file.ext(0, 1, 2, 3)
		
		texturePath = texturePath.trim();
		
		String path;
		String params;
		if (texturePath.indexOf('[') != -1)
		{
			final int split = texturePath.indexOf('[');
			path = texturePath.substring(0, split).trim();
			params = texturePath.substring(split).trim();
		}
		else if (texturePath.indexOf('(') != -1)
		{
			final int split = texturePath.indexOf('(');
			path = texturePath.substring(0, split).trim();
			params = texturePath.substring(split).trim();
		}
		else
		{
			path = texturePath;
			params = "";
		}

		return new TextureRequest(getTexturePathPrefix(path), params);
	}

	public String getTexturePathPrefix(String path) {
		if (path.contains("/") || path.contains("\\")) {
			return path;
		}

		String pathPrefix = "";
		if (path.equals("terrain") && version == VERSION_4) {  //MC 1.4 (or older) texture packs
			pathPrefix = "terrain.png";
		} else if (version == VERSION_5) { //MC 1.5 texture packs
			pathPrefix = "textures/blocks/" + path;
		}  else if ((version == VERSIONS_6_TO_8 || version == VERSIONS_9_TO_11 || version == VERSION_12)) { //MC 1.6-1.12 texture packs
			pathPrefix = "assets/minecraft/textures/blocks/" + path;
		} else if (version.getNumVersion() >= VERSION_13.getNumVersion()) { //MC 1.13+ texture packs
			pathPrefix = "assets/minecraft/textures/block/" + path;
		}

		return pathPrefix;
	}
	
	private PackTexture findTexture(TextureRequest request)
	{
		PackTexture tex = loadedPackTextures.get(request.path);
		
		if (tex == null)
		{
			BufferedImage img;
			try 
			{
				img = loadTexture(request.path);
				tex = new PackTexture(rasteriser, request.path, img);
				loadedPackTextures.put(request.path, tex);
			} 
			catch (FileNotFoundException e)
			{
				System.err.println("\nThe texture file '" + request.path + "' could not be found.");
			}
		}
		
		return tex;
	}
	
	public SubTexture findTexture(BufferedImage img, String path)
	{
		PackTexture tex = loadedPackTextures.get(path);
		
		if (tex == null)
		{			
			tex = new PackTexture(rasteriser, path, img);
			
			loadedPackTextures.put(path, tex);
		}
		
		return tex.getFullTexture();
	}
	
	public BufferedImage loadTexture(String path) throws FileNotFoundException
	{
		InputStream in = null;
		
		try
		{
			// Check texture pack and minecraft jar
			//ZipStack.ZipStackEntry entry = zipStack.getEntry(path);
			InputStream stream = zipStack.getStream(path);
			if (stream != null)
			{
				in = stream;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (in == null)
		{
			try
			{
				// Check classpath
				in = getClass().getClassLoader().getResourceAsStream(path);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if (in == null)
		{
			in = new FileInputStream(new File(path));
		}
		
		BufferedImage img = null;
		
		if (in != null)
		{
			try
			{
		
				img = ImageIO.read(in);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (Exception e) {}
			}
		}
		
		return copy( img );
	}
	
	public Map<String, BufferedImage> loadPatterns()
	{
		Map<String, BufferedImage> patterns = new HashMap<>();
		Map<String, String> codes = new HashMap<>();  // TODO: Maybe populate this map from defaultBlockConfig file?
		codes.put("banner_base.png", "base");
		codes.put("base.png", "baseMask");
		codes.put("border.png", "bo");
		codes.put("bricks.png", "bri");
		codes.put("circle.png", "mc");
		codes.put("creeper.png", "cre");
		codes.put("cross.png", "cr");
		codes.put("curly_border.png", "cbo");
		codes.put("diagonal_left.png", "ld");
		codes.put("diagonal_right.png", "rd");
		codes.put("diagonal_up_left.png", "lud");
		codes.put("diagonal_up_right.png", "rud");
		codes.put("flower.png", "flo");
		codes.put("gradient.png", "gra");
		codes.put("gradient_up.png", "gru");
		codes.put("half_horizontal.png", "hh");
		codes.put("half_horizontal_bottom.png", "hhb");
		codes.put("half_vertical.png", "vh");
		codes.put("half_vertical_right.png", "vhr");
		codes.put("mojang.png", "moj");
		codes.put("rhombus.png", "mr");
		codes.put("skull.png", "sku");
		codes.put("small_stripes.png", "ss");
		codes.put("square_bottom_left.png", "bl");
		codes.put("square_bottom_right.png", "br");
		codes.put("square_top_left.png", "tl");
		codes.put("square_top_right.png", "tr");
		codes.put("straight_cross.png", "sc");
		codes.put("stripe_bottom.png", "bs");
		codes.put("stripe_center.png", "cs");
		codes.put("stripe_downleft.png", "dls");
		codes.put("stripe_downright.png", "drs");
		codes.put("stripe_left.png", "ls");
		codes.put("stripe_middle.png", "ms");
		codes.put("stripe_right.png", "rs");
		codes.put("stripe_top.png", "ts");
		codes.put("triangle_bottom.png", "bt");
		codes.put("triangle_top.png", "tt");
		codes.put("triangles_bottom.png", "bts");
		codes.put("triangles_top.png", "tts");
		if (version.getNumVersion() >= VERSION_14.getNumVersion()) {
			codes.put("globe.png", "glb");
		}
			
		try (FileSystem fs = FileSystems.newFileSystem(Paths.get(zipStack.getBaseFileName()), null);
			DirectoryStream<Path> entries = Files.newDirectoryStream(fs.getPath("assets/minecraft/textures/entity/banner"));)
		{
			for (Path entry : entries)
				patterns.put(codes.get(entry.getFileName().toString()), loadTexture(entry.toString()));
			
			Path basePattern = fs.getPath("assets/minecraft/textures/entity/banner_base.png");
			patterns.put(codes.get(basePattern.getFileName().toString()), loadTexture(basePattern.toString()));
		}
		catch (IOException e)
		{
			System.out.println("No banner patterns found. You may be using an older Minecraft jar file");
		}

		return patterns;
	}
	
	private void loadBedTextures()
	{		
		try (FileSystem fs = FileSystems.newFileSystem(Paths.get(zipStack.getBaseFileName()), null);
				DirectoryStream<Path> entries = Files.newDirectoryStream(fs.getPath("assets/minecraft/textures/entity/bed"));)
		{
			for (Path entry : entries)
			{
				String filename = entry.getFileName().toString();
				String color = filename.substring(0, filename.lastIndexOf('.'));
				findTexture(loadTexture(entry.toString()), "bed_"+color);
			}
		}
		catch (IOException e)
		{
			log.error("No bed textures found. You may be using an older Minecraft jar file");
		}
	}

	private void loadPaintingTextures()
	{
		try (FileSystem fs = FileSystems.newFileSystem(Paths.get(zipStack.getBaseFileName()), null);
			 DirectoryStream<Path> entries = Files.newDirectoryStream(fs.getPath("assets/minecraft/textures/painting"));)
		{
			for (Path entry : entries)
			{
				String filename = entry.getFileName().toString();
				String name = filename.substring(0, filename.lastIndexOf('.'));
				findTexture(loadTexture(entry.toString()), name);
			}
		}
		catch (IOException e)
		{
			log.error("No painting textures found. You may be using an older Minecraft jar file");
		}
	}
	
	private void loadShulkerTextures()
	{		
		try (FileSystem fs = FileSystems.newFileSystem(Paths.get(zipStack.getBaseFileName()), null);
				DirectoryStream<Path> entries = Files.newDirectoryStream(fs.getPath("assets/minecraft/textures/entity/shulker"));)
		{
			for (Path entry : entries)
			{
				String filename = entry.getFileName().toString();
				if (filename.contains("shulker"))
				{
					String color = filename.substring(0, filename.lastIndexOf('.')).replace("shulker_", "");
					BufferedImage img = loadTexture(entry.toString());
					if (color.equals("shulker")) {
						findTexture(img, color);
					} else {
						findTexture(img, color + "_shulker");
					}
					
					int height = img.getHeight();
					int textureSize = height/4;
					int topTileHeight = (int) (height*(12.0f/64.0f));
					int bottomImgX = (int) (height*(48.0f/64.0f));
					int bottomImgY = (int) (height*(36.0f/64.0f));
					
					//Create side texture
					BufferedImage finalImage = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = finalImage.createGraphics();
					g.drawImage(img.getSubimage(0, textureSize, textureSize, topTileHeight), 0, 0, null);
					g.drawImage(img.getSubimage(bottomImgX, bottomImgY, textureSize, textureSize), 0, 0, null);
					if (color.equals("shulker")) {
						findTexture(finalImage, color + "_side");
					} else {
						findTexture(finalImage, color + "_shulker_side");
					}
				}
			}
		}
		catch (IOException e)
		{
			log.error("No shulker textures found. You may be using an older Minecraft jar file");
		}
	}
	
	public ZipStack getZipStack()
	{
		return zipStack;
	}

	public Version getVersion()
	{
		return version;
	}
	
	public Texture getVignetteTexture()
	{
		return vignetteTexture;
	}
	
	
	public SubTexture getSubTile(final int tileX, final int tileY)
	{
	//	return tiles.get( new Point(tileX, tileY) );
		return findTexture("terrain["+tileX+", "+tileY+"]");
	}
	
	
	public BufferedImage getItem(final int itemTileX, final int itemTileY)
	{
		// item sheet is a 16x16 grid of images, so figure out actual size in pixels
		
		final int itemWidth = itemSheet.getWidth() / 16;
		final int itemHeight = itemSheet.getHeight() / 16;
		
		final int x = itemTileX * itemWidth;
		final int y = itemTileY * itemHeight;
		return itemSheet.getSubimage(x, y, itemWidth, itemHeight);
	}
	
	public BufferedImage getItem(String fileName)
	{
		// in MC 1.5 items are already separate images, we load them so we can resize them
		try {
			return loadTexture(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public BufferedImage getIcon(final int virtualX, final int virtualY, final int width, final int height)
	{
		Point actual = iconVirtualToActual(virtualX, virtualY);
		Point size = iconVirtualToActual(width, height);
		
		return iconSheet.getSubimage(actual.x, actual.y, size.x, size.y);
	}
	
	public BufferedImage getChestImage()
	{
		int factor = (int) (chestImage.getWidth() / 256.0f);
		BufferedImage top = chestImage.getSubimage(0, 0, factor*176, factor*71);
		BufferedImage bottom = chestImage.getSubimage(0, factor*215, factor*176, factor*7);
		
		BufferedImage finalImg = new BufferedImage(factor*176, factor*78, BufferedImage.TYPE_INT_ARGB);
		finalImg.getGraphics().drawImage(top, 0, 0, null);
		finalImg.getGraphics().drawImage(bottom, 0, factor*71, null);
		return finalImg;
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public Color getGrassColour(final int x, final int y)
	{
		final int actualX = x & 0xff;
		final int actualY = y & 0xff;
		
		final int rgb = grassLookupImage.getRGB(actualX, actualY);
		return new Color(rgb);
		/*
		final float xPer = (float)actualX / (float)(grassLookupImage.getWidth()-1);
		final float yPer = (float)actualY / (float)(grassLookupImage.getHeight()-1);
		
		final int red = (int)(xPer * 255);
		final int green = (int)(yPer * 255);
		return new Color(red, green, 0); */
	}
	
	public Color getFoliageColour(final int x, final int y)
	{
		final int actualX = clamp(x, 0, foliageLookupImage.getWidth()-1);
		final int actualY = clamp(y, 0, foliageLookupImage.getHeight()-1);
		
		return new Color( foliageLookupImage.getRGB(actualX, actualY) );
	}
	
	private static int clamp(final int value, final int min, final int max)
	{
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}
	
	/** Makes a copy of the input image. Also converts to INT_ARGB so that we're always
	 *  working in the same colour space.
	 */
	public static BufferedImage copy(BufferedImage in)
	{
		if (in == null)
			return null;
		
		BufferedImage img = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (int x=0; x<in.getWidth(); x++)
		{
			for (int y=0; y<in.getHeight(); y++)
			{
				img.setRGB(x, y, in.getRGB(x, y));
			}
		}
		
		return img;		
	}	
	
	private Point iconVirtualToActual(final int virtualX, final int virtualY)
	{
		final float normX = (float)virtualX / 256.0f;
		final float normY = (float)virtualY / 256.0f;
		
		final int actualX = Math.round(normX * iconSheet.getWidth());
		final int actualY = Math.round(normY * iconSheet.getHeight());
		
		return new Point(actualX, actualY);
	}
}
