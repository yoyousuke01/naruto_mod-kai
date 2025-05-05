// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports

public static class ModelEyeball extends ModelBase {
	private final ModelRenderer bone;
	private final ModelRenderer octagon;
	private final ModelRenderer octagon_r1;
	private final ModelRenderer octagon_r2;
	private final ModelRenderer octagon_r3;
	private final ModelRenderer octagon2;
	private final ModelRenderer octagon_r4;
	private final ModelRenderer octagon_r5;
	private final ModelRenderer octagon_r6;
	private final ModelRenderer octagon3;
	private final ModelRenderer octagon_r7;
	private final ModelRenderer octagon_r8;
	private final ModelRenderer octagon_r9;
	private final ModelRenderer octagon4;
	private final ModelRenderer octagon_r10;
	private final ModelRenderer octagon_r11;
	private final ModelRenderer octagon_r12;

	public ModelEyeball() {
		textureWidth = 32;
		textureHeight = 32;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0F, 13.5F, 0.0F);

		octagon = new ModelRenderer(this);
		octagon.setRotationPoint(0.0F, 0.0F, 0.0F);
		bone.addChild(octagon);
		octagon.cubeList.add(new ModelBox(octagon, 0, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r1 = new ModelRenderer(this);
		octagon_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon.addChild(octagon_r1);
		setRotationAngle(octagon_r1, -2.3562F, 0.0F, 0.0F);
		octagon_r1.cubeList.add(new ModelBox(octagon_r1, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r2 = new ModelRenderer(this);
		octagon_r2.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon.addChild(octagon_r2);
		setRotationAngle(octagon_r2, -1.5708F, 0.0F, 0.0F);
		octagon_r2.cubeList.add(new ModelBox(octagon_r2, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r3 = new ModelRenderer(this);
		octagon_r3.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon.addChild(octagon_r3);
		setRotationAngle(octagon_r3, -0.7854F, 0.0F, 0.0F);
		octagon_r3.cubeList.add(new ModelBox(octagon_r3, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon2 = new ModelRenderer(this);
		octagon2.setRotationPoint(0.0F, 0.0F, 0.0F);
		bone.addChild(octagon2);
		setRotationAngle(octagon2, 0.0F, 0.0F, 0.7854F);

		octagon_r4 = new ModelRenderer(this);
		octagon_r4.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon2.addChild(octagon_r4);
		setRotationAngle(octagon_r4, -2.3562F, 0.0F, 0.0F);
		octagon_r4.cubeList.add(new ModelBox(octagon_r4, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r5 = new ModelRenderer(this);
		octagon_r5.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon2.addChild(octagon_r5);
		setRotationAngle(octagon_r5, -1.5708F, 0.0F, 0.0F);
		octagon_r5.cubeList.add(new ModelBox(octagon_r5, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r6 = new ModelRenderer(this);
		octagon_r6.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon2.addChild(octagon_r6);
		setRotationAngle(octagon_r6, -0.7854F, 0.0F, 0.0F);
		octagon_r6.cubeList.add(new ModelBox(octagon_r6, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon3 = new ModelRenderer(this);
		octagon3.setRotationPoint(0.0F, 0.0F, 0.0F);
		bone.addChild(octagon3);
		setRotationAngle(octagon3, 0.0F, 0.0F, 1.5708F);

		octagon_r7 = new ModelRenderer(this);
		octagon_r7.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon3.addChild(octagon_r7);
		setRotationAngle(octagon_r7, -2.3562F, 0.0F, 0.0F);
		octagon_r7.cubeList.add(new ModelBox(octagon_r7, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r8 = new ModelRenderer(this);
		octagon_r8.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon3.addChild(octagon_r8);
		setRotationAngle(octagon_r8, -1.5708F, 0.0F, 0.0F);
		octagon_r8.cubeList.add(new ModelBox(octagon_r8, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r9 = new ModelRenderer(this);
		octagon_r9.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon3.addChild(octagon_r9);
		setRotationAngle(octagon_r9, -0.7854F, 0.0F, 0.0F);
		octagon_r9.cubeList.add(new ModelBox(octagon_r9, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon4 = new ModelRenderer(this);
		octagon4.setRotationPoint(0.0F, 0.0F, 0.0F);
		bone.addChild(octagon4);
		setRotationAngle(octagon4, 0.0F, 0.0F, 2.3562F);

		octagon_r10 = new ModelRenderer(this);
		octagon_r10.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon4.addChild(octagon_r10);
		setRotationAngle(octagon_r10, -2.3562F, 0.0F, 0.0F);
		octagon_r10.cubeList.add(new ModelBox(octagon_r10, 14, 0, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r11 = new ModelRenderer(this);
		octagon_r11.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon4.addChild(octagon_r11);
		setRotationAngle(octagon_r11, -1.5708F, 0.0F, 0.0F);
		octagon_r11.cubeList.add(new ModelBox(octagon_r11, 0, 14, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));

		octagon_r12 = new ModelRenderer(this);
		octagon_r12.setRotationPoint(0.0F, 0.0F, 0.0F);
		octagon4.addChild(octagon_r12);
		setRotationAngle(octagon_r12, -0.7854F, 0.0F, 0.0F);
		octagon_r12.cubeList.add(new ModelBox(octagon_r12, 0, 7, -1.0F, -1.0F, -2.5F, 2, 2, 5, 0.06F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		bone.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
	}
}