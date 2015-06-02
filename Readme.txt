Application permettant de communiquer avec différents objets et capteurs Zwave.
Développer pour être utilisé sur un support android de type STB afin de pouvoir y connecter un controller usb Zwave.

_____________________________________________________________________
Système requis : Android API 14
Test de l'application effectué sur la version 4.2 d'Android (API 17)
_____________________________________________________________________


_____________________________________________________________________
Modifications appliquées à la bibliothèque initiale
_____________________________________________________________________

	- Documentation des différentes de la bibliothèque

	- Autorisation d'utilisation de clé usb Zwave (MainActivity.java)
	Création d'un BroadcastReceiver et d'un PendingIntent dans la méthode initUsbDriver(), utilisé au moment de la création de l'objet serialDriver (serialDriver = UsbSerialProber.acquire(usbManager, mPermissionIntent);) afin d'effectuer une demande de permission d'utilisation de la clé usb Zwave. Cela permet d'afficher un popup pour confirmer le droit d'utiliser ce périphérique. 

	- Création du fichier NodeGridAdapter.java permettant d'obtenir les différentes données renvoyées par les Nodes (objets Zwave enregistrés sur le réseau), d'obtenir les différentes fonctionnalités d'un Node et d'afficher les bons composants android pour effectuer actions qui lui correspondent.

	- Création du fichier Meter.java permettant de gérer les fonctions correspondant à un Node ayant la fonctionnalité de mesurer des valeurs (la consommation eléctrique d'une prise par exemple). La classe ne fonctionne pas correctement, elle ne permet pas d'obtenir la valeur de la consommation eléctrique.

	- Le système de détection des Nodes et des informations les concernants était fonctionnel dans la bibliothèque initiale, cependant certaines parties comme les informations contenues dans chaque Node n'était pas utilisée. Ayant commencé assez tard la modification de la bibliothèque à cause du problème de l'implémentation du driver java et par manque de temps, j'ai décidé de réutiliser ce système de détection des Nodes, mais d'implémenter les fonctions permettant d'utiliser les informations reçues de chaque Node qui n'était pas utilisé dans la bibliothèque initiale/





Descriptif de l'application
_____________________________________________________________________


Classes principales
-------------------

	MainActivity.java : 
		Activité lancée par défaut par le support Android, elle définie le layout de l’application, crée les objets et listeners nécessaire à la mise en place du réseau Z-wave.
	
	Manager.java : 
		Créé et initialisé dans MainActivity.java, cet objet créé le driver permettant à la STB d’utiliser le controller usb Z-wave, ainsi que le NodeManager qui permet de gérer les différents objets du réseau.
		
	Driver.java : 
		Met en place les communications entre le contrôleur (clé usb Z-wave) et les difféents objets Z- wave qui appartiennent au réseau, et contrôle les communications entre eux.
	
	NodeManage.java : 
		Créé les équipements Z-wave du réseau trouvé par le driver, chacun d’eux est représenté par un objet “Node”.
	
   	Node.java : 
   		Contient toutes les informations spécifique a un objet Z-wave du réseau.
   	
   	CommandClassManager.java : 
   		Contient toutes les commandes (CommandClasses) qu’un Node peut effectuer.
   	
   	CommandClass.java : 
   		Contient les différentes fonctionnalités qui peuvent être associés au différents objets Z-wave.

   	NodeGridAdapter.java :
   		Adapter de l'objet android GridView permettant à l'application d'afficher tous les Nodes alligné sur une grille à l'écran. C'est dans cette adapter que les Nodes sont analisé afin d'afficher les bons composants pour chaque Node
   		afin d'effectuer les bonnes actions.


Récupérer les informations d'un Node
------------------------------------

	La méthode createListValues() permet de trouver les types des données reçues par l'objet Zwave correspondant au Node.
	Elle prend en entrée l'objet ValueManager du Node sélectionné, il faut parcourir la liste des objets Value.java qu'il contient et comparer la variable ValueGenre avec les différents types disponibles (BOOL, BYTE, DECIMAL, INT, LIST...).
	Une fois le type trouvé il est alors possible de récupérer la valeur, le nom et l'unité de l'objet Value.java en le castant en son objet correspondant (ValueBool.java | ValueByte.java | ValueInt.java...).
 
	public synchronized void createListValues(LinearLayout listValues, ValueManager manager){
		Iterator it = manager.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Value> val = (Entry<Integer, Value>)it.next();
			Value tmp = val.getValue();
			if(tmp.getId().getGenre() == ValueId.ValueGenre.USER){
				tmp.setValueChangedListener(this);
				TextView value = new TextView(mContext);
				TextView label = new TextView(mContext);
				TextView unit = new TextView(mContext);
				// Find the type of the Value
				switch (tmp.getId().getType()) {
					case BOOL:
						value.setText(Boolean.toString( ((ValueBool)tmp).getValue() ));
						label.setText(((ValueBool)tmp).getLabel());
						unit.setText(((ValueBool)tmp).getUnits());
						break;
					case BYTE:
						value.setText(Byte.toString( ((ValueByte)tmp).getValue() ));
						label.setText(((ValueByte)tmp).getLabel());
						unit.setText(((ValueByte)tmp).getUnits());
						break;
					case DECIMAL:
						value.setText(((ValueDecimal)tmp).getValue());
						label.setText(((ValueDecimal)tmp).getLabel());
						unit.setText(((ValueDecimal)tmp).getUnits());
						break;
					case INT:
						value.setText(Integer.toString(((ValueInt)tmp).getValue()));
						label.setText(((ValueInt)tmp).getLabel());
						unit.setText(((ValueInt)tmp).getUnits());
						break;
					case LIST:
						
						break;
					case SCHEDULE:
						
						break;
					case SHORT:
						value.setText(Short.toString(((ValueShort)tmp).getValue()));
						label.setText(((ValueShort)tmp).getLabel());
						unit.setText(((ValueShort)tmp).getUnits());
						break;
					case STRING:
						value.setText(((ValueString)tmp).getValue());
						label.setText(((ValueString)tmp).getLabel());
						unit.setText(((ValueString)tmp).getUnits());
						break;
					case BUTTON:
						
						break;
					case RAW:
						
						break;
		
					default:
						break;
				}
				// Add the value, label and unit to a LinearLayout to display them nicely
				String adapt = value.getText().toString().replaceFirst("00000", "");
				value.setText(adapt);
				value.setTextColor(Color.BLACK);
				unit.setTextColor(Color.BLACK);
				label.setTextColor(Color.BLACK);
				LinearLayout container = new LinearLayout(mContext);
				container.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams containerParam = new LinearLayout.LayoutParams
						(LayoutParams.MATCH_PARENT, 0, 1.0f);
				containerParam.setMargins(0, 2, 0, 0);
				container.setBackgroundColor(Color.rgb(210,210,210));
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams
						(0, LayoutParams.MATCH_PARENT, 1.0f);
				container.setLayoutParams(containerParam);
				value.setLayoutParams(param);
				label.setLayoutParams(param);
				unit.setLayoutParams(param);
				container.addView(label);
				container.addView(value);
				container.addView(unit);
				listValues.addView(container);
			}
		}
	}


	Trouver le type d'un Node
	-------------------------

		Pour trouver le type d'un Node, c'est-à-dire, ses fonctionnalités, il faut regarder ce que l'objet contient comme objet CommandClass, pour y accéder il faut suivre la procédure suivante:

			if(node.getCommandClassManager().getCommandClass(SwitchBinary.COMMAND_CLASS_ID) != null){
				nodeSwitch.setVisibility(View.VISIBLE);
				nodeSwitch.setChecked(value.getValue() == 0 ? false : true);
				nodeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							node.setOn();
							Basic cc = (Basic) node.getCommandClassManager().getCommandClass(Basic.COMMAND_CLASS_ID);
							cc.set((byte)3);
						} else {
							node.setOff();
						}
					}
				});

		Ainsi si un Node supporte les fonctions d'un interrupteur (switch), il contient l'id de la CommandClass SwitchBinary.java dans son objet CommandClassManager. En effectuant ce genre de teste avec les différentes classes du package com.azwave.androidzwave.zwave.commandclass on peut savoir quelle fonctionnalités supporte un Node.


