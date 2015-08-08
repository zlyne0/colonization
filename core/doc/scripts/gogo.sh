#!/bin/bash

TEXTURE_PACKER="java -cp /opt/soft/libgdx-1.5.6/gdx.jar:/opt/soft/libgdx-1.5.6/extensions/gdx-tools/gdx-tools.jar com.badlogic.gdx.tools.texturepacker.TexturePacker"


CURRDIR=`pwd`
TERRAIN_DIR="rules/classic/resources/images/terrain"
OUTDIR="out"

RES_PRO="$CURRDIR/rules/classic/resources.properties"


function addSed {
	BEFORE=$1
	AFTER=$2

	# change / na \/ 
	# escape / for regexp for sed
	BEFORE=${BEFORE//\//\\/}
	AFTER=${AFTER//\//\\/}

	echo "s/$BEFORE/$AFTER/g" >> $CURRDIR/$OUTDIR/sedscript
}

function copyFilesInDirectory {
	SRC_DIR=$1
	LAYER=$2

	for i in $( ls -1 $CURRDIR/rules/classic/$SRC_DIR ); do
		cp $CURRDIR/rules/classic/$SRC_DIR/$i $OUTDIR/$LAYER
		REGION_NAME=${i//.png/}
		addSed "$SRC_DIR/$i" ":atlas:resources/images/$LAYER.atlas:$REGION_NAME"
	done	
}

rm -fr $OUTDIR

mkdir -p $OUTDIR/layer1
for i in $( ls -1 $CURRDIR/$TERRAIN_DIR ); do
	echo "terrain $i"

	if [ "$i" == "hills" ]
	then
		continue
	fi	
	if [ "$i" == "mountains" ]
	then
		continue
	fi	

	for f in $( ls -1 $CURRDIR/$TERRAIN_DIR/$i ); do
		TT_BEFORE="$TERRAIN_DIR/$i/$f"
		TT_AFTER="$OUTDIR/layer1/$i"_"$f"
		cp $TT_BEFORE $TT_AFTER
		REGION_NAME=${f//.png/}
		addSed "resources/images/terrain/$i/$f" ":atlas:resources/images/layer1.atlas:$i"_"$REGION_NAME"
	done	
done

echo "layer2"
mkdir -p $OUTDIR/layer2
copyFilesInDirectory "resources/images/terrain/hills" "layer2"
copyFilesInDirectory "resources/images/terrain/mountains" "layer2"
copyFilesInDirectory "resources/images/river" "layer2"

for i in $(ls -1 $CURRDIR/rules/classic/resources/images/forest ); do
	echo "forest $i"
	copyFilesInDirectory "resources/images/forest/$i" "layer2"
done

echo "layer3"
mkdir -p $OUTDIR/layer3
copyFilesInDirectory "resources/images/bonus" "layer3"
copyFilesInDirectory "resources/images/misc" "layer3"
copyFilesInDirectory "resources/images/settlements" "layer3"

cat $RES_PRO | sed -f $CURRDIR/$OUTDIR/sedscript >> $CURRDIR/$OUTDIR/resources.properties

cp $CURRDIR/pack.json $OUTDIR/layer1
cp $CURRDIR/pack.json $OUTDIR/layer2
cp $CURRDIR/pack.json $OUTDIR/layer3
$TEXTURE_PACKER $OUTDIR/layer1 $OUTDIR layer1
$TEXTURE_PACKER $OUTDIR/layer2 $OUTDIR layer2
$TEXTURE_PACKER $OUTDIR/layer3 $OUTDIR layer3


